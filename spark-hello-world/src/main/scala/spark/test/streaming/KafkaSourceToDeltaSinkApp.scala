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
 * 1. Nested fields not supported by merge.  Also Spark has limited support for predicate pushdown on nested fields.
 * 2. Every malformed row in the microbatch results in a "null" row being created in a new parquet file.
 *    Also, malformed rows are not captured in _currupt_record.
 * 3. If an existing row is updated, it is added to a new parquet file -- existing parquet files are never updated
 * 4. If an input microbatch contains duplicate rows, the entire job will be aborted. (Expected)
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
 * 14:35:37.917 [main] INFO org.apache.spark.sql.catalyst.expressions.codegen.CodeGenerator - Code generated in 4.740655 ms
 * +------+-----+----------+---------+
 * |userId|login|name_first|name_last|
 * +------+-----+----------+---------+
 * |0     |user0|007       |Bond     |
 * +------+-----+----------+---------+
 *
 * # Step 2: add wellformed users1-5
 * [20-01-20 14:37:18] /Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/streaming-sinks/KafkaSourceToDeltaSinkApp$/output.delta % ls -l
 * total 32
 * drwxr-xr-x  5 yzhang  CORP\Domain Users   160 Jan 20 14:36 _delta_log
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1101 Jan 20 14:35 part-00000-8655c425-5048-40e3-b8ae-19dfebe44a8f-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users   529 Jan 20 14:35 part-00000-a13cde32-0b22-4659-8216-feafb91d1e5b-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1123 Jan 20 14:36 part-00000-d4483623-4cc6-4193-ac10-f790b47b0d11-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1077 Jan 20 14:36 part-00001-e9f00cf0-97e9-4970-87fc-caec1836d0be-c000.snappy.parquet
 *
 * 14:37:56.074 [main] INFO org.apache.spark.sql.catalyst.expressions.codegen.CodeGenerator - Code generated in 4.093985 ms
 * +------+-----+----------+---------+
 * |userId|login|name_first|name_last|
 * +------+-----+----------+---------+
 * |0     |user0|007       |Bond     |
 * |1     |user1|Hello     |Kitty    |
 * |2     |user2|Mickey    |Mouse    |
 * |3     |user3|Minie     |Mouse    |
 * |4     |user4|Garfield  |null     |
 * |5     |user5|null      |null     |
 * +------+-----+----------+---------+
 *
 * # Step 3: add/update users with 2 malformed rows
 * [20-01-20 14:40:15] /Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/streaming-sinks/KafkaSourceToDeltaSinkApp$/output.delta % ls -l
 * total 48
 * drwxr-xr-x  6 yzhang  CORP\Domain Users   192 Jan 20 14:39 _delta_log
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1101 Jan 20 14:35 part-00000-8655c425-5048-40e3-b8ae-19dfebe44a8f-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users   529 Jan 20 14:35 part-00000-a13cde32-0b22-4659-8216-feafb91d1e5b-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1123 Jan 20 14:36 part-00000-d4483623-4cc6-4193-ac10-f790b47b0d11-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1154 Jan 20 14:39 part-00000-fe864f3b-aa5e-450e-b417-10f2149449ef-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1092 Jan 20 14:39 part-00001-dddca5a8-8cde-43c4-b2f9-5892bb6aa575-c000.snappy.parquet
 * -rw-r--r--  1 yzhang  CORP\Domain Users  1077 Jan 20 14:36 part-00001-e9f00cf0-97e9-4970-87fc-caec1836d0be-c000.snappy.parquet
 *
 * 14:41:02.705 [main] INFO org.apache.spark.sql.catalyst.expressions.codegen.CodeGenerator - Code generated in 4.2893 ms
 * +------+-----+----------------+---------+
 * |userId|login|name_first      |name_last|
 * +------+-----+----------------+---------+
 * |null  |null |null            |null     |
 * |null  |null |null            |null     |
 * |0     |user0|007             |Bond     |
 * |1     |user1|Hello           |Kitty    |
 * |2     |user2|Mickey          |Mouse    |
 * |3     |user3|Minie           |Mouse    |
 * |4     |user4|Garfield Updated|null     |
 * |5     |user5|Tiger           |null     |
 * |6     |user6|Big Bird        |null     |
 * +------+-----+----------------+---------+
 *
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
      .select($"userId", $"login", $"name.first".as("name_first"), $"name.last".as("name_last"))
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
          "name_last" -> "updates.name_last"))
      .whenNotMatched
      .insertExpr(
        Map(
          "userId" -> "updates.userId",
          "login" -> "updates.login",
          "name_first" -> "updates.name_first",
          "name_last" -> "updates.name_last"))
      .execute()
  }
}
