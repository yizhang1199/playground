package spark.test.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import scala.concurrent.duration._
import scala.reflect.io.{Directory, File}

object FileSourceToParquetSinkApp extends App {
  private val name: String = FileSourceToParquetSinkApp.getClass.getSimpleName
  private val numberOfCores: Int = 2

  val sourceSchema = StructType(
    List(
      StructField("userId", IntegerType, nullable = false),
      StructField("login", StringType, nullable = false),
      StructField("name", StringType, nullable = false)
    )
  )

  val spark = SparkSession.builder()
    .appName("FileSourceToParquetSinkApp")
    .master(s"local[$numberOfCores]")
    .getOrCreate()
  spark.conf.set("spark.sql.shuffle.partitions", s"$numberOfCores")

  val sourcePath = setupSourcePath()
  val streamingDF = spark
    .readStream // Returns DataStreamReader
    .option("maxFilesPerTrigger", 2) // Force processing of only 1 file per trigger
    .schema(sourceSchema) // Required for all streaming DataFrames
    .json(sourcePath) // The stream's source directory and file type

  streamingDF.printSchema()

  val outputBase = setupSinkBase()
  val sinkPath = outputBase + "output.parquet"        // A subdirectory for our output
  val checkpointPath = outputBase + "checkpoint"      // A subdirectory for our checkpoint & W-A logs

  val streamingQuery = streamingDF                     // Start with our "streaming" DataFrame
    .writeStream                                       // Get the DataStreamWriter
    .queryName(s"$name-1s")                // Name the query
    .trigger(Trigger.ProcessingTime(1.seconds))        // Configure for a 1-second micro-batch
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("parquet")                        // Specify the sink type, a Parquet file
    .option("checkpointLocation", checkpointPath)      // Specify the location of checkpoint files & W-A logs
    .outputMode("append")                 // Write only new data to the "file"
    .start(sinkPath)                                   // Start the job, writing to the specified directory
    .awaitTermination


  private def setupSinkBase(): String = {
    val outputBase = s"target/streaming-sinks/$name-2/"
    val dir = Directory(File(outputBase))
    dir.deleteRecursively()
    dir.createDirectory(failIfExists = false)
    outputBase
  }

  private def setupSourcePath(): String = {
    val sourcePath = "target/streaming-sources/json"
    val dir = Directory(File(sourcePath))
    dir.deleteRecursively()
    dir.createDirectory(failIfExists = false)
    sourcePath
  }
}
