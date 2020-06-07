package spark.test

import org.apache.spark.sql.SaveMode

object HiveTableApp extends App {
  val name = HiveTableApp.getClass.getSimpleName
  implicit val spark = SparkHelper.initSpark(name)

  val things = new Things()

  // bucketing is only supported with managed hive tables
  things.uniform.write
    .mode(SaveMode.Overwrite)
    .partitionBy("color")
    .bucketBy(2,"age") // Spark will create numBuckets * numCores files, e.g. 8 (2 * 4) in this case
    .saveAsTable("bucketedTable")
    //.parquet(s"$DataPath/partitionByAge_bucketByColor") // fails with 'save' does not support bucketBy right now;

  val bucketedTable = spark.catalog.getTable("bucketedTable")

  println("databases: " + spark.catalog.listDatabases())
  println(s"bucketedTable=$bucketedTable")
}