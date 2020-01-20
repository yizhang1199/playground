package spark.test.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StringType

import scala.concurrent.duration._

/**
 * How to deserialize keys and values from Kafka streams: when using Spark, keys and values are always deserialized
 * as byte arrays with ByteArrayDeserializer. Use DataFrame operations to explicitly deserialize the values.
 *
 * https://docs.databricks.com/spark/latest/structured-streaming/kafka.html
 * https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html
 */
object KafkaSourceToParquetSinkApp extends App {
  private val name: String = KafkaSourceToParquetSinkApp.getClass.getSimpleName
  private val numberOfCores: Int = 2

  val spark = SparkSession.builder()
    .appName(name)
    .master(s"local[$numberOfCores]")
    .getOrCreate()
  spark.conf.set("spark.sql.shuffle.partitions", s"$numberOfCores")

  import spark.implicits._

  val jsonOptions = Map(
    "mode" -> "PERMISSIVE",
    "columnNameOfCorruptRecord" -> "CorruptRecord")

  /**
   * Kafka source has a fixed schema and cannot be set with a custom one:
   *
   * root
   * |-- key: binary (nullable = true)
   * |-- value: binary (nullable = true)
   * |-- topic: string (nullable = true)
   * |-- partition: integer (nullable = true)
   * |-- offset: long (nullable = true)
   * |-- timestamp: timestamp (nullable = true)
   * |-- timestampType: integer (nullable = true)
   */
  val streamingDF = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "MyTopic")
    .load()
    .select($"value".cast(StringType))
    .select(from_json($"value", StreamingSource.jsonSourceSchema, jsonOptions).as("data"))
    .select("data.*", "*")
    .drop("data")

  /**
   * root
   * |-- data: struct (nullable = true)
   * |    |-- userId: integer (nullable = true)
   * |    |-- login: string (nullable = true)
   * |    |-- name: struct (nullable = true)
   * |    |    |-- first: string (nullable = true)
   * |    |    |-- last: string (nullable = true)
   * |    |-- CorruptRecord: string (nullable = true)
   */
  println("====================================")
  streamingDF.printSchema()

  val sinkPath = StreamingSink.sinkPath(name, "parquet") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  val streamingQuery = streamingDF
    .writeStream
    .queryName(name)
    .trigger(Trigger.ProcessingTime(1.seconds))
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("parquet")
    .option("checkpointLocation", checkpointPath) // Specify the location of checkpoint files & W-A logs
    .outputMode("append") // Write only new data to the "file"
    .start(sinkPath) // Start the job, writing to the specified directory
    .awaitTermination
}
