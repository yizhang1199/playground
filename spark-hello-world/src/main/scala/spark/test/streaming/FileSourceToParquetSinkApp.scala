package spark.test.streaming

import org.apache.spark.sql.{DataFrame, SparkSession}
import spark.test.streaming.StreamingSource.jsonSourcePath
import spark.test.SparkHelper

object FileSourceToParquetSinkApp extends App {
  private val name: String = FileSourceToParquetSinkApp.getClass.getSimpleName

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  /**
   * Updates to existing files will NOT be detected.
   * New files added to the directory will be picked up - a new parquet file will be created, even with duplicate rows,
   * e.g. both the old and new parquet files may have a row with the same id and potentially different data.
   *
   * Malformed rows will be stored in the "_corrupt_record" column.
   * Rows with any valid columns will be preserved
   * Rows with duplicate IDs will be preserved
   *
   * scala> val usersDf = spark.read.parquet("<sinkPath>")
   * scala> usersDf.createOrReplaceTempView("users")
   * scala> spark.sql("select * from users").show(20, false)
   * +------+---------------+---------------------+--------------------------------------------------------------+
   * |userId|login          |name                 |_corrupt_record                                               |
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
  val streamingDF: DataFrame = spark
    .readStream
    .option("maxFilesPerTrigger", 2)
    .options(SparkHelper.corruptRecordOptions)
    .schema(SparkHelper.userSchema)
    .json(jsonSourcePath)

  streamingDF.printSchema()

  StreamingSink.writeToParquetSink(name, streamingDF)
}
