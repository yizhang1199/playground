package spark.test.streaming

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StringType
import spark.test.SparkHelper
import spark.test.SparkHelper.{corruptRecordOptions, userSchema}

import scala.concurrent.duration._

/**
 * See KafkaSourceToDeltaSinkApp for other Delta behaviors
 *
 * Kafka commands:
 * bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic UsersTopic
 * bin/kafka-topics.sh --list --bootstrap-server localhost:9092
 * bin/kafka-console-producer.sh --broker-list localhost:9092 --topic UsersTopic
 *
 * +------+-------+-------------+---------------+
 * |userId|login  |name         |_corrupt_record|
 * +------+-------+-------------+---------------+
 * |0     |user007|[James, Bond]|null           |
 * +------+-------+-------------+---------------+
 *
 * +------+-------+---------------+---------------+
 * |userId|login  |name           |_corrupt_record|
 * +------+-------+---------------+---------------+
 * |0     |user007|[James, Bond]  |null           |
 * |1     |user1  |[Hello, Kitty] |null           |
 * |2     |user2  |[Mickey, Mouse]|null           |
 * |3     |user3  |[Minie, Mouse] |null           |
 * |4     |user4  |[Garfield,]    |null           |
 * |5     |user5  |null           |null           |
 * +------+-------+---------------+---------------+
 *
 * After publishing some corrupt events to the topic -- none is stored in _corrupt_record
 * +------+-------------+---------------+---------------+
 * |userId|login        |name           |_corrupt_record|
 * +------+-------------+---------------+---------------+
 * |null  |null         |null           |null           |
 * |null  |null         |null           |null           |
 * |null  |missingUserId|null           |null           |
 * |0     |user007      |[James, Bond]  |null           |
 * |1     |user1        |[Hello, Kitty] |null           |
 * |2     |user2        |[Mickey, Mouse]|null           |
 * |3     |user3        |[Minie, Mouse] |null           |
 * |4     |user4        |[Garfield,]    |null           |
 * |5     |user5        |null           |null           |
 * +------+-------------+---------------+---------------+
 *
 */
object KafkaSourceNestedFieldsToDeltaSinkApp extends App {
  private val name: String = KafkaSourceNestedFieldsToDeltaSinkApp.getClass.getSimpleName

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  import spark.implicits._

  val sinkPath = StreamingSink.sinkPath(name, "delta") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  // create the delta table path otherwise streaming fails with "..." is not a Delta table
  // TODO what do we do in PROD with live streams?
  val initialUsersDf = SparkHelper.readJson(filename = "userInit.json", schema = userSchema)
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
    .select(from_json($"value", userSchema, corruptRecordOptions).as("data"))
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
          "name.first" -> "updates.name.first",
          "name.last" -> "updates.name.last",
          "_corrupt_record" -> null))
      .whenNotMatched
      .insertAll()
      .execute()
  }

  def upsert2(microBatchDF: DataFrame, batchId: Long): Unit = {
    // https://docs.databricks.com/delta/delta-update.html#upsert-into-a-table-using-merge
    // API doc: https://docs.delta.io/0.5.0/api/scala/io/delta/tables/index.html
    DeltaTable.forPath(spark, sinkPath)
      .as("events")
      .merge(
        microBatchDF.as("updates"),
        "events.userId = updates.userId")
      .whenMatched
      .updateAll()
      .whenNotMatched
      .insertAll()
      .execute()
  }
}
