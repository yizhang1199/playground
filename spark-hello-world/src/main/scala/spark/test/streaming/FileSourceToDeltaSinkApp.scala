package spark.test.streaming

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.streaming.Trigger
import spark.test.streaming.StreamingSource.jsonSourcePath
import spark.test.SparkHelper

import scala.concurrent.duration._
/**
 * https://docs.databricks.com/delta/delta-streaming.html
 */
object FileSourceToDeltaSinkApp extends App {
  private val name: String = FileSourceToDeltaSinkApp.getClass.getSimpleName
  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  val sinkPath = StreamingSink.sinkPath(name, "delta") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  val initialUsersDf = SparkHelper.readJson(filename = "userInit.json")
  initialUsersDf  // create the delta table path otherwise streaming fails with "..." is not a Delta table
    .write
    .format("delta")
    .mode(SaveMode.Overwrite)
    .save(sinkPath)

  val streamingDF = spark
    .readStream
    .option("maxFilesPerTrigger", 1)
    .options(SparkHelper.corruptRecordOptions)
    .schema(SparkHelper.userSchema)
    .json(jsonSourcePath)

  streamingDF.printSchema()

  val streamingQuery = streamingDF
    .writeStream
    .queryName(name)
    .trigger(Trigger.ProcessingTime(1.seconds))        // Configure for a 1-second micro-batch
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("delta")                        // Specify the sink type, a Parquet file
    .option("checkpointLocation", checkpointPath)      // Specify the location of checkpoint files & W-A logs
    .foreachBatch(upsert _)
    .start()
    .awaitTermination

  def upsert(microBatchDF: DataFrame, batchId: Long): Unit = {
    // available since databricks 5.5:
    microBatchDF.createOrReplaceTempView("updates")
    //    microBatchDF.sparkSession.sql(s"""
    //    MERGE INTO events t
    //    USING updates s
    //    ON s.userId = t.userId
    //    WHEN MATCHED THEN UPDATE SET *
    //    WHEN NOT MATCHED THEN INSERT *
    //  """)

    DeltaTable.forPath(spark, sinkPath)
      .as("events")
      .merge(
        microBatchDF.as("updates"),
        "events.userId = updates.userId")
      .whenMatched
      .updateExpr(
        Map(
          "login" -> "updates.login",
          "name" -> "updates.name"))
      .whenNotMatched
      .insertExpr(
        Map(
          "userId" -> "updates.userId",
          "login" -> "updates.login",
          "name" -> "updates.name"))
      .execute()
  }
}
