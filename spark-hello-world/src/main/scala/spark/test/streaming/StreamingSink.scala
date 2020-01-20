package spark.test.streaming

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.Trigger

import scala.concurrent.duration._
import scala.reflect.io.{Directory, File}

object StreamingSink {

   private def setupSinkBase(appName: String): String = {
    val outputBase = s"target/streaming-sinks/$appName/"
    val dir = Directory(File(outputBase))
    dir.deleteRecursively()
    dir.createDirectory(failIfExists = false)
    outputBase
  }

  def sinkPath(appName: String, format: String): String = {
    setupSinkBase(appName) + "output." + format
  }

  def checkpointPath(appName: String): String = {
    setupSinkBase(appName) + "checkpoint"
  }

  def writeToParquetSink(appName: String, streamingDF: DataFrame): Unit = {
    val format = "parquet"
    streamingDF
      .writeStream
      .queryName(appName)
      .trigger(Trigger.ProcessingTime(1.seconds))
      //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
      .format(format)
      .option("checkpointLocation", checkpointPath(appName)) // Specify the location of checkpoint files & W-A logs
      .outputMode("append") // Write only new data to the "file"
      .start(sinkPath(appName, format)) // Start the job, writing to the specified directory
      .awaitTermination
  }
}
