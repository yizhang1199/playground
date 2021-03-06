package spark.test

import org.apache.spark.sql.{SaveMode, SparkSession}

object PartitionWithAppendApp extends App {

  val name = PartitionWithAppendApp.getClass.getSimpleName
  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  private val DataPath = "target/partition-append-test"
  //Directory(File(DataPath)).deleteRecursively()

  import spark.implicits._

  val things = new Things().uniform

  /**
   * 2 parquet files, sorted & evenly clustered by age.
   *
   * /Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/partition-append-test % ls -l
   * total 48
   * -rw-r--r--  1 yzhang  CORP\Domain Users     0 Jan  5 22:47 _SUCCESS
   * -rw-r--r--  1 yzhang  CORP\Domain Users  8921 Jan  5 22:47 part-00000-084cb216-cc3d-4160-add0-499491175a98-c000.snappy.parquet (age: 1-6)
   * -rw-r--r--  1 yzhang  CORP\Domain Users  9208 Jan  5 22:47 part-00001-084cb216-cc3d-4160-add0-499491175a98-c000.snappy.parquet (age: 7-12)
   */
  things.repartitionByRange(2, $"age")
    .write
    .mode(SaveMode.Overwrite)
    .parquet(DataPath)

  /**
   * 3 new parquet files were added to the same directory.  The original parquet files were untouched.
   *
   * /Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/partition-append-test % ls -l
   * total 96
   * -rw-r--r--  1 yzhang  CORP\Domain Users     0 Jan  5 22:50 _SUCCESS
   * -rw-r--r--  1 yzhang  CORP\Domain Users  8921 Jan  5 22:47 part-00000-084cb216-cc3d-4160-add0-499491175a98-c000.snappy.parquet
   * -rw-r--r--  1 yzhang  CORP\Domain Users  6544 Jan  5 22:50 part-00000-d758e15b-5282-44a2-bbcc-d95c61dd06ef-c000.snappy.parquet (age: 1-4)
   * -rw-r--r--  1 yzhang  CORP\Domain Users  9208 Jan  5 22:47 part-00001-084cb216-cc3d-4160-add0-499491175a98-c000.snappy.parquet
   * -rw-r--r--  1 yzhang  CORP\Domain Users  6649 Jan  5 22:50 part-00001-d758e15b-5282-44a2-bbcc-d95c61dd06ef-c000.snappy.parquet (age: 5-8)
   * -rw-r--r--  1 yzhang  CORP\Domain Users  6578 Jan  5 22:50 part-00002-d758e15b-5282-44a2-bbcc-d95c61dd06ef-c000.snappy.parquet (age: 9-12)
   */
  things.repartitionByRange(3, $"age")
    .write
    .mode(SaveMode.Append)
    .parquet(DataPath)

  /**
   * Overwrote existing files with 4 new files, each with 3 distinct ages.
   *
   * /Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/partition-append-test % ls -l
   * total 32
   * -rw-r--r--  1 yzhang  CORP\Domain Users     0 Jun 25 18:37 _SUCCESS
   * -rw-r--r--  1 yzhang  CORP\Domain Users  1619 Jun 25 18:37 part-00000-33f1ba90-8a32-4d9c-a25c-518b0fe330c6-c000.snappy.parquet (age: 1-3)
   * -rw-r--r--  1 yzhang  CORP\Domain Users  1841 Jun 25 18:37 part-00001-33f1ba90-8a32-4d9c-a25c-518b0fe330c6-c000.snappy.parquet (age: 4-6)
   * -rw-r--r--  1 yzhang  CORP\Domain Users  1887 Jun 25 18:37 part-00002-33f1ba90-8a32-4d9c-a25c-518b0fe330c6-c000.snappy.parquet (age: 7-9)
   * -rw-r--r--  1 yzhang  CORP\Domain Users  1610 Jun 25 18:37 part-00003-33f1ba90-8a32-4d9c-a25c-518b0fe330c6-c000.snappy.parquet (age: 10-12)
   */
  things.repartitionByRange(4, $"age")
    .write
    .mode(SaveMode.Overwrite)
    .parquet(DataPath)
}