package spark.test.streaming

import scala.reflect.io.{Directory, File}

object StreamingSink {

   private def setupSinkBase(name: String): String = {
    val outputBase = s"target/streaming-sinks/$name/"
    val dir = Directory(File(outputBase))
    dir.deleteRecursively()
    dir.createDirectory(failIfExists = false)
    outputBase
  }

  def sinkPath(name: String, sinkType: String): String = {
    setupSinkBase(name) + "output." + sinkType
  }

  def checkpointPath(name: String): String = {
    setupSinkBase(name) + "checkpoint"
  }
}
