package spark.test.streaming

import scala.reflect.io.{Directory, File}

object StreamingSource {

  val jsonSourcePath: String = StreamingSource.setupSourcePath("json")

  def setupSourcePath(subPath: String = ""): String = {
    val sourcePath = s"target/streaming-sources/$subPath"
    val dir = Directory(File(sourcePath))
    dir.deleteRecursively()
    dir.createDirectory(failIfExists = false)
    sourcePath
  }
}
