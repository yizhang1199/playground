package spark.test.streaming

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import spark.test.SparkHelper
import spark.test.SparkHelper.corruptRecordOptions

import scala.concurrent.duration._

/**
 * Delta Behaviors
 * 1. Nested fields not supported by merge
 * 2. If a microbatch contains a single malformed row, 1 empty parquet file will be created.  Why?
 * 3. If an existing row is updated, it is added to a new parquet file -- existing parquet files are never updated
 * 4. If an input microbatch contains duplicate rows, the entire job will be aborted.
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
 */
object KafkaSourceToDeltaSinkApp extends App {
  private val name: String = KafkaSourceToDeltaSinkApp.getClass.getSimpleName
  private val flattenedUserSchema: StructType = StructType(
    List(
      StructField("userId", IntegerType, nullable = false),
      StructField("login", StringType, nullable = false),
      StructField("name_first", StringType, nullable = false),
      StructField("name_last", StringType, nullable = false)
    )
  )

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  import spark.implicits._

  val sinkPath = StreamingSink.sinkPath(name, "delta") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  // create the delta table path otherwise streaming fails with "..." is not a Delta table
  val initialUsersDf = SparkHelper.readJson(filename = "userInit.json")
      .select($"userId", $"login", $"name.first".as("name_first"), $"name.last".as("name_last"))

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
    //.select(to_json($"data", jsonOptions).as("json"))
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
    microBatchDF.createOrReplaceTempView("updates")

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
