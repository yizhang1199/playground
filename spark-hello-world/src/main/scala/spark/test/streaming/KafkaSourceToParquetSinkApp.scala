package spark.test.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import scala.concurrent.duration._

object KafkaSourceToParquetSinkApp extends App {
  private val name: String = KafkaSourceToParquetSinkApp.getClass.getSimpleName
  private val numberOfCores: Int = 2

  val spark = SparkSession.builder()
    .appName(name)
    .master(s"local[$numberOfCores]")
    .getOrCreate()
  spark.conf.set("spark.sql.shuffle.partitions", s"$numberOfCores")

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
    .option("mode", "PERMISSIVE")
    .option("columnNameOfCorruptRecord", "CorruptRecord")
    .load()
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")

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
