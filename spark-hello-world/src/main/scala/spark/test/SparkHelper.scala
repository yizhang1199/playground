package spark.test

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{ArrayType, DateType, IntegerType, StringType, StructField, StructType}
import spark.test.PartitionApp.spark

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

    spark.sparkContext.setLogLevel("WARN") // spark is to verbose

    spark
  }

  def readJson(filename: String, schema: StructType, multiLine: Boolean = false)
              (implicit spark: SparkSession): DataFrame = {
    val jsonDf = spark
      .read
      .option("allowComments", "true")
      .option("allowNumericLeadingZeros", "true")
      .options(corruptRecordOptions)
      .option("multiLine", multiLine.toString)
      .option("dateFormat", "yyyy-MM-dd")
      .schema(schema)
      .json(this.getClass.getClassLoader.getResource(s"json/$filename").getFile)

    jsonDf
  }

  val flattenedUserSchema: StructType = StructType(
    List(
      StructField("userId", IntegerType, nullable = false),
      StructField("login", StringType, nullable = false),
      StructField("name_first", StringType, nullable = false),
      StructField("name_last", StringType, nullable = false),
      StructField("_corrupt_record", StringType, nullable = true)
    )
  )

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

  val personSchema: StructType = StructType(
    List(
      StructField("name", StringType, nullable = false),
      StructField("age", IntegerType, nullable = true),
      StructField("address",
        StructType(List(
          StructField("city", StringType, nullable = true),
          StructField("street", StringType, nullable = true),
          StructField("country", StringType, nullable = true)
        )),
        nullable = true),
      StructField("children",
        ArrayType(
          StructType(List(
            StructField("name", StringType, nullable = true),
            StructField("age", IntegerType, nullable = true),
            StructField("birthdate", DateType, nullable = true)
          ))
        ),
        nullable = true),
      StructField("_corrupt_record", StringType, nullable = true)
    )
  )
}
