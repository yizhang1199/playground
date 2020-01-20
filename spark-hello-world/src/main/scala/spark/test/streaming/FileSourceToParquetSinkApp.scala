package spark.test.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import scala.concurrent.duration._

import StreamingSource.{jsonSourceSchema, jsonSourcePath}

object FileSourceToParquetSinkApp extends App {
  private val name: String = FileSourceToParquetSinkApp.getClass.getSimpleName
  private val numberOfCores: Int = 2

  val spark = SparkSession.builder()
    .appName(name)
    .master(s"local[$numberOfCores]")
    .getOrCreate()
  spark.conf.set("spark.sql.shuffle.partitions", s"$numberOfCores")

  /**
   * Updates to existing files will NOT be detected.
   * New files added to the directory will be picked up - a new parquet file will be created, even with duplicate rows,
   * e.g. both the old and new parquet files may have a row with the same id and potentially different data.
   *
   * Malformed rows will be stored in the "CorruptRecord" column.
   * Rows with any valid columns will be preserved
   * Rows with duplicate IDs will be preserved
   *
   * scala> val usersDf = spark.read.parquet("<sinkPath>")
   * scala> usersDf.createOrReplaceTempView("users")
   * scala> spark.sql("select * from users").show(20, false)
   * +------+---------------+---------------------+--------------------------------------------------------------+
   * |userId|login          |name                 |CorruptRecord                                                 |
   * +------+---------------+---------------------+--------------------------------------------------------------+
   * |1     |user1          |Hello Kitty          |null                                                          |
   * |2     |user2          |Mickey               |null                                                          |
   * |3     |user3          |Minie                |null                                                          |
   * |null  |null           |null                 |{"userId":  "malformed-userId", "blah": "I'm an invalid row" }|
   * |null  |missingUserId  |null                 |null                                                          |
   * |1     |user1-duplicate|Hello Kitty Duplicate|null                                                          |
   * |null  |null           |null                 |I'm another corrupt row with no userId.                       |
   * +------+---------------+---------------------+--------------------------------------------------------------+
   */
  val streamingDF = spark
    .readStream
    .option("maxFilesPerTrigger", 2)
    .option("mode", "PERMISSIVE")
    .option("columnNameOfCorruptRecord", "CorruptRecord")
    .schema(jsonSourceSchema) // Required for file streaming DataFrames
    .json(jsonSourcePath) // The stream's source directory and file type

  streamingDF.printSchema()

  val sinkPath = StreamingSink.sinkPath(name, "parquet") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  val streamingQuery = streamingDF
    .writeStream
    .queryName(name)
    .trigger(Trigger.ProcessingTime(1.seconds))
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("parquet") // Specify the sink type, a Parquet file
    .option("checkpointLocation", checkpointPath) // Specify the location of checkpoint files & W-A logs
    .outputMode("append") // Write only new data to the "file"
    .start(sinkPath) // Start the job, writing to the specified directory
    .awaitTermination
}
