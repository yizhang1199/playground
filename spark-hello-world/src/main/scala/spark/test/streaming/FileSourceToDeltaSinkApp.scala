package spark.test.streaming

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.streaming.Trigger
import spark.test.streaming.StreamingSource.jsonSourcePath
import spark.test.SparkHelper
import spark.test.SparkHelper.userSchema

import scala.concurrent.duration._
/**
 * https://docs.databricks.com/delta/delta-streaming.html
 */
object FileSourceToDeltaSinkApp extends App {
  private val name: String = FileSourceToDeltaSinkApp.getClass.getSimpleName
  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  import spark.implicits._

  val sinkPath = StreamingSink.sinkPath(name, "delta") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  // TODO what do we do in PROD with live streams?
  // create the delta table path otherwise streaming fails with "..." is not a Delta table
  val initialUsersDf = SparkHelper.readJson(filename = "userInit.json", schema = userSchema)
    .select($"userId", $"login", $"name.first".as("name_first"), $"name.last".as("name_last"), $"_corrupt_record")
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

  streamingDF
    .select($"userId", $"login", $"name.first".as("name_first"), $"name.last".as("name_last"), $"_corrupt_record")
    .writeStream
    .queryName(name)
    .trigger(Trigger.ProcessingTime(1.seconds))
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("delta")
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
