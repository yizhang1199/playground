package spark.test

import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.reflect.io.{Directory, File}

object HiveTableApp extends App {

  // warehouseLocation points to the default location for managed databases and tables
  private val warehouseLocation = "target/spark-warehouse"
  Directory(File(warehouseLocation)).deleteRecursively()

  val spark = SparkSession.builder()
    .config("spark.sql.warehouse.dir", warehouseLocation)
    .appName("RangePartitionApp")
    .master("local[4]")
    //.enableHiveSupport()
    .getOrCreate()

  val things = Things.setup(spark)

  // bucketing is only supported with managed hive tables
  things.write
    .mode(SaveMode.Overwrite)
    .partitionBy("color")
    .bucketBy(2,"age") // Spark created 8 part files!  Why?
    .saveAsTable("bucketedTable")
    //.parquet(s"$DataPath/partitionByAge_bucketByColor") // fails with 'save' does not support bucketBy right now;

  val bucketedTable = spark.catalog.getTable("bucketedTable")

  println("databases: " + spark.catalog.listDatabases())
  println(s"bucketedTable=$bucketedTable")
}