package spark.test.streaming

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import scala.reflect.io.{Directory, File}

object StreamingSource {

  val jsonSourcePath: String = StreamingSource.setupSourcePath("json")

  val jsonSourceSchema: StructType = StructType(
    List(
      StructField("userId", IntegerType, nullable = false),
      StructField("login", StringType, nullable = false),
      StructField("name",
        StructType(List(
          StructField("first", StringType, nullable = true),
          StructField("last", StringType, nullable = true)
        )),
        nullable = true),
      StructField("CorruptRecord", StringType, nullable = true)
    )
  )

  def setupSourcePath(subPath: String = ""): String = {
    val sourcePath = s"target/streaming-sources/$subPath"
    val dir = Directory(File(sourcePath))
    dir.deleteRecursively()
    dir.createDirectory(failIfExists = false)
    sourcePath
  }
}
