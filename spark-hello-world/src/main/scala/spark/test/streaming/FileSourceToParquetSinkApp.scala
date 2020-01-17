package spark.test.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import scala.concurrent.duration._

import StreamingSource.{jsonSourceSchema, jsonSourcePath}

object FileSourceToParquetSinkApp extends App {
  private val name: String = FileSourceToParquetSinkApp.getClass.getSimpleName
  private val numberOfCores: Int = 2

  val spark = SparkSession.builder()
    .appName("FileSourceToParquetSinkApp")
    .master(s"local[$numberOfCores]")
    .getOrCreate()
  spark.conf.set("spark.sql.shuffle.partitions", s"$numberOfCores")

  /**
   * Updates to existing files will NOT be detected.
   * New files added to the directory will be picked up - a new parquet file will be created, even with duplicate rows,
   * e.g. both the old and new parquet files may have a row with the same id and potentially different data.
   */
  val streamingDF = spark
    .readStream // Returns DataStreamReader
    .option("maxFilesPerTrigger", 2) // Force processing of only 1 file per trigger
    .option("mode", "PERMISSIVE")
    .option("columnNameOfCorruptRecord", "BadRecord")
    .schema(jsonSourceSchema) // Required for all streaming DataFrames
    .json(jsonSourcePath) // The stream's source directory and file type

  streamingDF.printSchema()

  val sinkPath = StreamingSink.sinkPath(name, "parquet") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  val streamingQuery = streamingDF // Start with our "streaming" DataFrame
    .writeStream // Get the DataStreamWriter
    .queryName(s"$name-1s") // Name the query
    .trigger(Trigger.ProcessingTime(1.seconds)) // Configure for a 1-second micro-batch
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("parquet") // Specify the sink type, a Parquet file
    .option("checkpointLocation", checkpointPath) // Specify the location of checkpoint files & W-A logs
    .outputMode("append") // Write only new data to the "file"
    .start(sinkPath) // Start the job, writing to the specified directory
    .awaitTermination
}
