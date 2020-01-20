package spark.test

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import scala.reflect.io.{Directory, File}

object SparkHelper {

  private val numberOfCores: Int = 2

  val corruptRecordOptions = Map(
    "mode" -> "PERMISSIVE",
    "columnNameOfCorruptRecord" -> "_corrupt_record")

  def initSpark(name: String): SparkSession = {
    // warehouseLocation points to the default location for managed databases and tables
    val warehouseLocation = "target/spark-warehouse"
    Directory(File(warehouseLocation)).deleteRecursively()

    val spark = SparkSession.builder()
      .appName(name)
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .master(s"local[$numberOfCores]")
      .getOrCreate()
    spark.conf.set("spark.sql.shuffle.partitions", s"$numberOfCores")
    // java.lang.NoSuchMethodError: com.google.common.base.Stopwatch.elapsedMillis()J
    // https://github.com/googleapis/google-cloud-java/issues/4414  TODO is this the right way?
    //spark.conf.set("spark.executor.userClassPathFirst", "true")
    //spark.conf.set("spark.driver.userClassPathFirst", "true")

    spark
  }

  def readJson(filename: String, multiLine: Boolean = false)
              (implicit spark: SparkSession): DataFrame = {
    val jsonDf = spark
      .read
      .option("allowComments", "true")
      .option("allowNumericLeadingZeros", "true")
      .options(corruptRecordOptions)
      .option("multiLine", multiLine.toString)
      .option("dateFormat", "yyyy-MM-dd")
      //.schema(schema)
      .json(this.getClass.getClassLoader.getResource(s"json/$filename").getFile)

    jsonDf
  }

  val userSchema: StructType = StructType(
    List(
      StructField("userId", IntegerType, nullable = false),
      StructField("login", StringType, nullable = false),
      StructField("name",
        StructType(List(
          StructField("first", StringType, nullable = true),
          StructField("last", StringType, nullable = true)
        )),
        nullable = true),
      StructField("_corrupt_record", StringType, nullable = true)
    )
  )
}
