package spark.test.streaming

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StringType
import spark.test.SparkHelper
import spark.test.SparkHelper.{corruptRecordOptions, flattenedUserSchema, userSchema}

import scala.concurrent.duration._

/**
 * Delta Behaviors
 * 1. Nested fields not supported by DeltaTable.merge.  Also Spark has limited support for predicate pushdown on nested fields.
 * 2. Every malformed row in the microbatch results in a "null" row being created in a new parquet file.
 *    Also, malformed rows are not captured in _currupt_record.
 * 3. If an existing row is updated, it is added to a new parquet file -- existing parquet files are never updated. (Expected)
 * 4. If an input microbatch contains duplicate rows, the entire job will be aborted. (Expected)
 * 5. When configuring a Kafka source to write to a delta streaming sink, using an empty directory will fail because the empty directory "is not a Delta table".
 *    But before the source stream is written to the delta sink, we will have an empty directory.  What's the right way to deal with this?
 *
 * How to deserialize keys and values from Kafka streams: when using Spark, keys and values are always deserialized
 * as byte arrays with ByteArrayDeserializer. Use DataFrame operations to explicitly deserialize the values.
 *
 * https://docs.databricks.com/spark/latest/structured-streaming/kafka.html
 * https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html
 *
 * from_json returns wrong result if corrupt record column is in the middle of schema
 * https://issues.apache.org/jira/browse/SPARK-25952
 *
 * Start kafka locally: http://kafka.apache.org/quickstart
 *
 * Delta merge does not support nested fields
 * org.apache.spark.sql.AnalysisException: Nested field is not supported in the INSERT clause of MERGE operation (field = `name`.`first`).;
 * at org.apache.spark.sql.delta.DeltaErrors$.nestedFieldNotSupported(DeltaErrors.scala:471)
 * at org.apache.spark.sql.delta.PreprocessTableMerge.$anonfun$apply$8(PreprocessTableMerge.scala:78)
 * at org.apache.spark.sql.delta.PreprocessTableMerge.$anonfun$apply$8$adapted(PreprocessTableMerge.scala:74)
 * at scala.collection.Iterator.foreach(Iterator.scala:941)
 *
 * java.lang.UnsupportedOperationException: Cannot perform MERGE as multiple source rows matched and attempted to update the same
 * target row in the Delta table. By SQL semantics of merge, when multiple source rows match
 * on the same target row, the update operation is ambiguous as it is unclear which source
 * should be used to update the matching target row.
 * You can preprocess the source table to eliminate the possibility of multiple matches.
 * Please refer to https://docs.delta.io/latest/delta/delta-update.html#upsert-into-a-table-using-merge
 * at org.apache.spark.sql.delta.DeltaErrors$.multipleSourceRowMatchingTargetRowInMergeException(DeltaErrors.scala:451)
 *
 * Output from reading the delta tables:
 * # Step 1: start KafkaSourceToDeltaSinkApp and add user0 as seed data
 * +------+-------+----------+---------+---------------+
 * |userId|login  |name_first|name_last|_corrupt_record|
 * +------+-------+----------+---------+---------------+
 * |0     |user007|James     |Bond     |null           |
 * +------+-------+----------+---------+---------------+
 *
 * # Step 2: add wellformed users1-5
 * +------+-------+----------+---------+---------------+
 * |userId|login  |name_first|name_last|_corrupt_record|
 * +------+-------+----------+---------+---------------+
 * |0     |user007|James     |Bond     |null           |
 * |1     |user1  |Hello     |Kitty    |null           |
 * |2     |user2  |Mickey    |Mouse    |null           |
 * |3     |user3  |Minie     |Mouse    |null           |
 * |4     |user4  |Garfield  |null     |null           |
 * |5     |user5  |null      |null     |null           |
 * +------+-------+----------+---------+---------------+
 *
 * # Step 3: add/update users with 2 malformed rows
 * +------+-------+----------------+---------+---------------+
 * |userId|login  |name_first      |name_last|_corrupt_record|
 * +------+-------+----------------+---------+---------------+
 * |null  |null   |null            |null     |null           |
 * |null  |null   |null            |null     |null           |
 * |0     |user007|James           |Bond     |null           |
 * |1     |user1  |Hello           |Kitty    |null           |
 * |2     |user2  |Mickey          |Mouse    |null           |
 * |3     |user3  |Minie           |Mouse    |null           |
 * |4     |user4  |Garfield Updated|null     |null           |
 * |5     |user5  |Tiger           |null     |null           |
 * |6     |user6  |Big Bird        |null     |null           |
 * +------+-------+----------------+---------+---------------+
 *
 * [20-01-21 17:37:11] /Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/streaming-sinks/KafkaSourceToDeltaSinkApp$/output.delta % ls -l
 * (Both spark.sql.shuffle.partitions and number of cores were set to 2.  Everytime the microbatch has >= 2 events, 2 parquet files will be created)
 * drwxr-xr-x  6 yzhang  CORP\Domain Users   192 Jan 21 17:34 _delta_log
 * -rw-r--r--  1 yzhang  CORP\Domain Users   632 Jan 21 17:19 part-00000-0a176e0f-a466-475c-8bb3-330dc440b627-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1299 Jan 21 17:34 part-00000-a639de4b-f89d-4e95-bd50-8a62ae2bd235-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1286 Jan 21 17:19 part-00000-d3464348-3963-4ab4-ab35-077b808ced0a-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1268 Jan 21 17:21 part-00000-e10e8eb5-006e-4ef1-82ae-db688879c8d7-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1222 Jan 21 17:21 part-00001-4288faf2-cf99-47b4-b96f-64eac4c112cf-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1239 Jan 21 17:34 part-00001-b2444e5e-4d40-4ca1-92bd-22da3aa2b91c-c000.snappy.parquet
 */
object KafkaSourceToDeltaSinkApp extends App {
  private val name: String = KafkaSourceToDeltaSinkApp.getClass.getSimpleName

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  import spark.implicits._

  val sinkPath = StreamingSink.sinkPath(name, "delta") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  // create the delta table path otherwise streaming fails with "..." is not a Delta table
  // TODO what do we do in PROD with live streams?
  val initialUsersDf = SparkHelper.readJson(filename = "userInit.json", schema = userSchema)
      .select($"userId", $"login", $"name.first".as("name_first"), $"name.last".as("name_last"), $"_corrupt_record")
  // TODO if we include "_corrupt_record" in the schema, then in merge, we have to specify how "_corrupt_record" will be updated.
  // TODO By the time merge is called, wouldn't it be too late? How do we know what value to set?

  println("===============initialUsersDf===============")
  initialUsersDf.printSchema()
  initialUsersDf
    .write
    .format("delta")
    .mode(SaveMode.Overwrite)
    .save(sinkPath)

  /**
   * Read from Kafka source, which has a fixed schema
   */
  val streamingDF = spark // TODO from_json does not work with columnNameOfCorruptRecord -- bad rows are dropped
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "UsersTopic")
    .load()
    .select($"value".cast(StringType))
    .select(from_json($"value", flattenedUserSchema, corruptRecordOptions).as("data"))
    .select("data.*", "*") // TODO is this the best way to explode a StructType column?
    .drop("data")

  /**
   * root
   * |-- data: struct (nullable = true)
   * |    |-- userId: integer (nullable = true)
   * |    |-- login: string (nullable = true)
   * |    |-- name: struct (nullable = true)
   * |    |    |-- first: string (nullable = true)
   * |    |    |-- last: string (nullable = true)
   * |    |-- _corrupt_record: string (nullable = true)
   */
  println("====================================")
  streamingDF.printSchema()

  streamingDF
    .writeStream
    .queryName(name)
    .trigger(Trigger.ProcessingTime(1.seconds)) // Configure for a 1-second micro-batch
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("delta") // Specify the sink type, a Parquet file
    .option("checkpointLocation", checkpointPath) // Specify the location of checkpoint files & W-A logs
    .foreachBatch(upsert _)
    .start()
    .awaitTermination

  def upsert(microBatchDF: DataFrame, batchId: Long): Unit = {
    // https://docs.databricks.com/delta/delta-update.html#upsert-into-a-table-using-merge
    // API doc: https://docs.delta.io/0.5.0/api/scala/io/delta/tables/index.html
    DeltaTable.forPath(spark, sinkPath)
      .as("events")
      .merge(
        microBatchDF.as("updates"),
        "events.userId = updates.userId")
      .whenMatched
      .updateExpr(
        Map(
          "login" -> "updates.login",
          "name_first" -> "updates.name_first",
          "name_last" -> "updates.name_last",
          "_corrupt_record" -> "updates._corrupt_record"))
      .whenNotMatched
      .insertExpr(
        Map(
          "userId" -> "updates.userId",
          "login" -> "updates.login",
          "name_first" -> "updates.name_first",
          "name_last" -> "updates.name_last",
          "_corrupt_record" -> "updates._corrupt_record"))
      .execute()
  }
}
