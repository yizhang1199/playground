package spark.test

import java.sql.Date

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._

import scala.reflect.io.{Directory, File}
import scala.util.Random

object PartitionApp extends App {

  val name = PartitionApp.getClass.getSimpleName
  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  private val DataPath = "target/partition-test"
  Directory(File(DataPath)).deleteRecursively()

  import spark.implicits._

  private val things = new Things()

  //clusterSkewedThings()
  testRepartition()

  private def saltAge: UserDefinedFunction = udf { (age: Int, createDate: Date) =>
    val count = 10
    val createDateMs = createDate.getTime
    //println(s"createDateMs=$createDateMs") //1553410800000
    age match {
      case 1 =>
        //createDateMs % (count * 60) // 60%
        Random.nextInt(count * 60) // 60%
      case 2 | 4 | 5 =>
        //(count * 60) + createDateMs % (count * 20) // 20%
        (count * 60) + Random.nextInt(count * 20) // 20%
      case 3 =>
        //(count * 80) + createDateMs % (count * 20) // 20%
        (count * 80) + Random.nextInt(count * 20) // 20%
    }
  }

  private def clusterSkewedThings() = {
    println("----------------------------after salting")
    val df = things.skewed.sort("age") // TODO without sort saltAge distributes 0-9 across all ages, why?
      .withColumn("salt", saltAge($"age", $"createDate"))

    df.groupBy("age")
      .agg(min("salt"), max("salt"))
      .show(truncate = false)

    df.repartitionByRange(5, $"salt")
      //.drop("salt")
      .write
      .mode(SaveMode.ErrorIfExists)
      .parquet(s"$DataPath/repartitionByRange_skewedAgeSalt")
  }

  private def testRepartition() {
    /**
     * 4 parquet files created directly under ".../repartitionByRange4_id/", sorted & clustered by id.  Distribution is
     * even since total # of records created are multiples of 4.
     *
     * total 32
     * -rw-r--r--  1 yzhang  CORP\Domain Users     0 Jan  5 17:06 _SUCCESS
     * -rw-r--r--  1 yzhang  CORP\Domain Users  1650 Jan  5 17:06 part-00000-df67530d-1259-400d-b92d-bd2782c161ea-c000.snappy.parquet
     * -rw-r--r--  1 yzhang  CORP\Domain Users  1650 Jan  5 17:06 part-00001-df67530d-1259-400d-b92d-bd2782c161ea-c000.snappy.parquet
     * -rw-r--r--  1 yzhang  CORP\Domain Users  1650 Jan  5 17:06 part-00002-df67530d-1259-400d-b92d-bd2782c161ea-c000.snappy.parquet
     * -rw-r--r--  1 yzhang  CORP\Domain Users  1675 Jan  5 17:06 part-00003-df67530d-1259-400d-b92d-bd2782c161ea-c000.snappy.parquet
     */
    things.uniform.repartitionByRange(4, $"id")
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionByRange4_id")

    // Also evenly distributed, each file has 2 colors.
    things.uniform.repartitionByRange(3, $"color")
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionByRange3_color")

    /**
     * data skewed since each file contains a single age
     *
     * file1 (for age=1): row group 1: RC:78 TS:2261 OFFSET:4
     * file2 (for age=2): row group 1: RC:12 TS:607 OFFSET:4
     * file3 (for age=3): row group 1: RC:28 TS:1027 OFFSET:4
     * file4 (for age=4): row group 1: RC:1 TS:336 OFFSET:4
     * file5 (for age=5): row group 1: RC:1 TS:311 OFFSET:4
     */
    things.skewed.repartitionByRange(5, $"age")
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionByRange_skewed_age")

    /**
     * row group 1: RC:24 TS:929 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:172/255/1.48 VC:24 ENC:BIT_PACKED,PLAIN ST:[min: 4, max: 112, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:176 SZ:176/339/1.93 VC:24 ENC:RLE,BIT_PACKED,PLAIN ST:[min: Thing 10, max: Thing 97, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:352 SZ:68/64/0.94 VC:24 ENC:BIT_PACKED,PLAIN_DICTIONARY ST:[min: 1, max: 1, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:420 SZ:126/122/0.97 VC:24 ENC:RLE,BIT_PACKED,PLAIN_DICTIONARY ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:546 SZ:147/149/1.01 VC:24 ENC:RLE,BIT_PACKED,PLAIN ST:[min: 2019-01-02, max: 2019-04-29, num_nulls: 0]
     *
     * row group 1: RC:18 TS:777 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:148/207/1.40 VC:18 ENC:BIT_PACKED,PLAIN ST:[min: 1, max: 117, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:152 SZ:143/263/1.84 VC:18 ENC:RLE,BIT_PACKED,PLAIN ST:[min: Thing 1, max: Thing 87, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:295 SZ:68/64/0.94 VC:18 ENC:BIT_PACKED,PLAIN_DICTIONARY ST:[min: 1, max: 1, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:363 SZ:112/109/0.97 VC:18 ENC:RLE,BIT_PACKED,PLAIN_DICTIONARY ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:475 SZ:138/134/0.97 VC:18 ENC:RLE,BIT_PACKED,PLAIN_DICTIONARY ST:[min: 2019-05-24, max: 2019-08-03, num_nulls: 0]
     *
     * row group 1: RC:32 TS:1129 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:204/319/1.56 VC:32 ENC:BIT_PACKED,PLAIN ST:[min: 19, max: 120, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:208 SZ:208/440/2.12 VC:32 ENC:RLE,BIT_PACKED,PLAIN ST:[min: Thing 101, max: Thing 99, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:416 SZ:68/64/0.94 VC:32 ENC:BIT_PACKED,PLAIN_DICTIONARY ST:[min: 1, max: 1, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:484 SZ:128/125/0.98 VC:32 ENC:RLE,BIT_PACKED,PLAIN_DICTIONARY ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:612 SZ:174/181/1.04 VC:32 ENC:RLE,BIT_PACKED,PLAIN ST:[min: 2019-08-10, max: 2019-11-20, num_nulls: 0]
     *
     * row group 1: RC:22 TS:897 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:164/239/1.46 VC:22 ENC:PLAIN,BIT_PACKED ST:[min: 7, max: 119, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:168 SZ:167/317/1.90 VC:22 ENC:PLAIN,BIT_PACKED,RLE ST:[min: Thing 107, max: Thing 94, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:335 SZ:82/78/0.95 VC:22 ENC:PLAIN_DICTIONARY,BIT_PACKED ST:[min: 1, max: 3, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:417 SZ:125/122/0.98 VC:22 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:542 SZ:144/141/0.98 VC:22 ENC:PLAIN,BIT_PACKED,RLE ST:[min: 2019-01-03, max: 2019-12-23, num_nulls: 0]
     *
     * row group 1: RC:24 TS:944 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:172/255/1.48 VC:24 ENC:PLAIN,BIT_PACKED ST:[min: 2, max: 116, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:176 SZ:175/340/1.94 VC:24 ENC:PLAIN,BIT_PACKED,RLE ST:[min: Thing 100, max: Thing 96, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:351 SZ:82/78/0.95 VC:24 ENC:PLAIN_DICTIONARY,BIT_PACKED ST:[min: 3, max: 5, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:433 SZ:126/122/0.97 VC:24 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:559 SZ:142/149/1.05 VC:24 ENC:PLAIN,BIT_PACKED,RLE ST:[min: 2019-01-22, max: 2019-12-14, num_nulls: 0]
     */
    things.skewed.repartitionByRange(5, $"age", $"createDate")
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionByRange_skewed_age_createDate")

    /**
     * 4 parquet files created directly under ".../repartitionByRange4_date_color/", sorted & clustered by createDate then color.
     * The data is clustered perfectly along the first dimension (e.g. createDate), but almost not at all along further
     * dimensions (e.g. color). This is expected for linear sorting.
     *
     * parquet-tools meta part-00000-181643e3-bbc3-4230-86ba-ffc71f15c305-c000.snappy.parquet
     * row group 1: RC:31 TS:1174 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:200/311/1.56 VC:31 ENC:PLAIN,BIT_PACKED ST:[min: 1, max: 109, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:204 SZ:200/426/2.13 VC:31 ENC:PLAIN,BIT_PACKED,RLE ST:[min: Thing 1, max: Thing 95, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:404 SZ:128/124/0.97 VC:31 ENC:PLAIN_DICTIONARY,BIT_PACKED ST:[min: 1, max: 12, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:532 SZ:129/125/0.97 VC:31 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:661 SZ:193/188/0.97 VC:31 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: 2019-01-02, max: 2019-04-09, num_nulls: 0]
     *
     * -- partial meta data for the rest of the parquet files
     * part-00001-181643e3-bbc3-4230-86ba-ffc71f15c305-c000.snappy.parquet
     * color:        BINARY SNAPPY DO:0 FPO:631 SZ:133/131/0.98 VC:43 ENC:BIT_PACKED,PLAIN_DICTIONARY,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:764 SZ:208/225/1.08 VC:43 ENC:BIT_PACKED,PLAIN,RLE ST:[min: 2019-04-13, max: 2019-07-04, num_nulls: 0]
     *
     * part-00002-181643e3-bbc3-4230-86ba-ffc71f15c305-c000.snappy.parquet
     * color:        BINARY SNAPPY DO:0 FPO:387 SZ:126/122/0.97 VC:17 ENC:RLE,BIT_PACKED,PLAIN_DICTIONARY ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:513 SZ:119/121/1.02 VC:17 ENC:RLE,BIT_PACKED,PLAIN ST:[min: 2019-07-08, max: 2019-09-20, num_nulls: 0]
     *
     * part-00003-181643e3-bbc3-4230-86ba-ffc71f15c305-c000.snappy.parquet
     * color:        BINARY SNAPPY DO:0 FPO:514 SZ:129/125/0.97 VC:29 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:643 SZ:161/169/1.05 VC:29 ENC:PLAIN,BIT_PACKED,RLE ST:[min: 2019-09-23, max: 2019-12-30, num_nulls: 0]
     */
    things.uniform.repartitionByRange(4, $"createDate", $"color")
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionByRange4_date_color")

    /**
     * Each partition is represented using a subdirectory.  Nested directories are used for multiple partitions, in this
     * case, a total of 6 * 12 = 72 directories will be created (if there are enough data). Example:
     * .../partitionBy_color_age/color=blue/
     * .../partitionBy_color_age/color=blue/age=1
     * .../partitionBy_color_age/color=blue/age=2
     * .../partitionBy_color_age/color=blue/age=...
     * .../partitionBy_color_age/color=green/...
     *
     * The "numPartitions" passed to repartitionByRange determines how many part files will be created in each
     * directory that contains data (e.g. .../color=blue/age=1/ will have 2 part files).
     *
     * Note that columns used for partitioning are removed from parquet files, example:
     * parquet-tools meta ./color=blue/age=2/part-00003-2144c590-08cd-4ab2-a675-6a5d1ef74ad1.c000.snappy.parquet
     * row group 1: RC:1 TS:208 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:71/69/0.97 VC:1 ENC:PLAIN,BIT_PACKED ST:[min: 110, max: 110, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:75 SZ:86/84/0.98 VC:1 ENC:PLAIN,BIT_PACKED,RLE ST:[min: Thing 110, max: Thing 110, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:161 SZ:57/55/0.96 VC:1 ENC:PLAIN,BIT_PACKED,RLE ST:[min: 2019-02-01, max: 2019-02-01, num_nulls: 0]
     */
    things.uniform
      .repartitionByRange(2, $"createDate") // 2 part files created per bottom level directory
      .write
      .partitionBy("color", "age") // creates 6 * 12 = 72 directories (6 colors, and 12 ages, each color has 12 directories, 1 for each age)
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/partitionBy_color_age")

    /**
     * Input Data: (colors = blue, green, purple, read, turquise, yellow) (age = 1-12) (totalRowCount = 120)
     *
     * 3 parquet files are created directly under ".../repartitionBy_color/".  Data are distributed based on
     * hash(color) % numPartitions, which can create data skews.
     *
     * part-00000-a1afeeaa-1285-4911-8c4c-844d15a2b727-c000.snappy.parquet (colors: blue)
     * row group 1: RC:20 TS:837 OFFSET:4
     *
     * part-00001-a1afeeaa-1285-4911-8c4c-844d15a2b727-c000.snappy.parquet (colors: green & turquoise)
     * row group 1: RC:40 TS:1345 OFFSET:4
     *
     * part-00002-a1afeeaa-1285-4911-8c4c-844d15a2b727-c000.snappy.parquet (colors: red, purple, yellow)
     * row group 1: RC:60 TS:1848 OFFSET:4
     */
    things.uniform.repartition(3, $"color") // hash partition "color" into 3 buckets
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionBy_color")

    /**
     * 6 colors with 6 partitions but only 5 files were created. Likely 2 colors hashed into the same bucket.
     * One file had 2 colors (purple & red), the rest files have 1 color each.
     */
    things.uniform.repartition(6, $"color") // hash partition "color" into 6 buckets
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionBy_color6")

    // Exception in thread "main" org.apache.spark.sql.AnalysisException: 'save' does not support bucketBy right now;
    //  things.write
    //    .bucketBy(3, "color") // hash "color" into 3 buckets
    //    .mode(SaveMode.Overwrite)
    //    .parquet(s"$DataPath/bucketBy_color")

    /**
     * Input: (colors = blue, green, purple, read, turquise, yellow) (age = 1-12) (totalRowCount = 120)
     *
     * 3 (aka numPartitions) parquet files are created directly under ".../repartitionBy_color_age/".
     * Data are distributed based on the hash of partitionExpressions over numPartitions, e.g. hash(color, age) % 3
     * All rows where `expressions` evaluate to the same values are guaranteed to be in the same partition.
     *
     * $key.hashCode () % numPartitions.  (Output taken when using 120 rows)
     *
     * parquet-tools meta part-00000-9a090e3f-bd72-43f2-809f-b2504c6c4cb6-c000.snappy.parquet
     * row group 1: RC:38 TS:1331 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:228/367/1.61 VC:38 ENC:BIT_PACKED,PLAIN ST:[min: 2, max: 120, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:232 SZ:231/511/2.21 VC:38 ENC:BIT_PACKED,PLAIN,RLE ST:[min: Thing 102, max: Thing 99, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:463 SZ:124/120/0.97 VC:38 ENC:BIT_PACKED,PLAIN_DICTIONARY ST:[min: 1, max: 12, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:587 SZ:132/128/0.97 VC:38 ENC:BIT_PACKED,PLAIN_DICTIONARY,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:719 SZ:209/205/0.98 VC:38 ENC:BIT_PACKED,PLAIN,RLE ST:[min: 2019-01-05, max: 2019-12-28, num_nulls: 0]
     *
     * parquet-tools meta part-00001-9a090e3f-bd72-43f2-809f-b2504c6c4cb6-c000.snappy.parquet
     * row group 1: RC:51 TS:1656 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:280/471/1.68 VC:51 ENC:PLAIN,BIT_PACKED ST:[min: 1, max: 116, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:284 SZ:277/662/2.39 VC:51 ENC:PLAIN,BIT_PACKED,RLE ST:[min: Thing 1, max: Thing 97, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:561 SZ:136/132/0.97 VC:51 ENC:PLAIN_DICTIONARY,BIT_PACKED ST:[min: 1, max: 12, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:697 SZ:137/134/0.98 VC:51 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:834 SZ:261/257/0.98 VC:51 ENC:PLAIN,BIT_PACKED,RLE ST:[min: 2019-01-02, max: 2019-12-05, num_nulls: 0]
     *
     * part-00002-9a090e3f-bd72-43f2-809f-b2504c6c4cb6-c000.snappy.parquet
     * row group 1: RC:31 TS:1154 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:200/311/1.56 VC:31 ENC:BIT_PACKED,PLAIN ST:[min: 7, max: 118, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:204 SZ:204/426/2.09 VC:31 ENC:RLE,BIT_PACKED,PLAIN ST:[min: Thing 10, max: Thing 98, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:408 SZ:128/124/0.97 VC:31 ENC:BIT_PACKED,PLAIN_DICTIONARY ST:[min: 1, max: 12, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:536 SZ:119/116/0.97 VC:31 ENC:RLE,BIT_PACKED,PLAIN_DICTIONARY ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:655 SZ:180/177/0.98 VC:31 ENC:RLE,BIT_PACKED,PLAIN ST:[min: 2019-01-14, max: 2019-12-29, num_nulls: 0]
     */
    things.uniform.repartition(3, $"color", $"age") // hash partition "color" into 3 buckets
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartitionBy_color_age")

    /**
     * Input: (colors = blue, green, purple, read, turquise, yellow) (age = 1-12) (totalRowCount = 120)
     *
     * 3 (aka numPartitions) parquet files are created directly under ".../repartition_RR/".  Data are evenly distributed
     * across $numPartitions files using RoundRobinPartitioning
     *
     * parquet-tools meta part-00000-49e2caf6-cfa6-4cf0-a018-82c6692485e8-c000.snappy.parquet
     * row group 1: RC:40 TS:1383 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:236/383/1.62 VC:40 ENC:PLAIN,BIT_PACKED ST:[min: 5, max: 116, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:240 SZ:244/531/2.18 VC:40 ENC:PLAIN,BIT_PACKED,RLE ST:[min: Thing 109, max: Thing 97, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:484 SZ:132/128/0.97 VC:40 ENC:PLAIN_DICTIONARY,BIT_PACKED ST:[min: 1, max: 12, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:616 SZ:131/128/0.98 VC:40 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:747 SZ:217/213/0.98 VC:40 ENC:PLAIN,BIT_PACKED,RLE ST:[min: 2019-01-03, max: 2019-12-22, num_nulls: 0]
     *
     * part-00001-49e2caf6-cfa6-4cf0-a018-82c6692485e8-c000.snappy.parquet
     * row group 1: RC:40 TS:1389 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:236/383/1.62 VC:40 ENC:BIT_PACKED,PLAIN ST:[min: 2, max: 120, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:240 SZ:245/537/2.19 VC:40 ENC:RLE,BIT_PACKED,PLAIN ST:[min: Thing 100, max: Thing 92, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:485 SZ:132/128/0.97 VC:40 ENC:BIT_PACKED,PLAIN_DICTIONARY ST:[min: 1, max: 12, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:617 SZ:131/128/0.98 VC:40 ENC:RLE,BIT_PACKED,PLAIN_DICTIONARY ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:748 SZ:215/213/0.99 VC:40 ENC:RLE,BIT_PACKED,PLAIN ST:[min: 2019-01-05, max: 2019-12-07, num_nulls: 0]
     *
     * part-00002-49e2caf6-cfa6-4cf0-a018-82c6692485e8-c000.snappy.parquet
     * row group 1: RC:40 TS:1380 OFFSET:4
     * --------------------------------------------------------------------------------
     * id:           INT64 SNAPPY DO:0 FPO:4 SZ:236/383/1.62 VC:40 ENC:PLAIN,BIT_PACKED ST:[min: 1, max: 118, num_nulls: 0]
     * name:         BINARY SNAPPY DO:0 FPO:240 SZ:237/532/2.24 VC:40 ENC:PLAIN,BIT_PACKED,RLE ST:[min: Thing 1, max: Thing 99, num_nulls: 0]
     * age:          INT32 SNAPPY DO:0 FPO:477 SZ:128/124/0.97 VC:40 ENC:PLAIN_DICTIONARY,BIT_PACKED ST:[min: 1, max: 12, num_nulls: 0]
     * color:        BINARY SNAPPY DO:0 FPO:605 SZ:132/128/0.97 VC:40 ENC:PLAIN_DICTIONARY,BIT_PACKED,RLE ST:[min: blue, max: yellow, num_nulls: 0]
     * createDate:   INT32 SNAPPY DO:0 FPO:737 SZ:217/213/0.98 VC:40 ENC:PLAIN,BIT_PACKED,RLE ST:[min: 2019-01-14, max: 2019-12-28, num_nulls: 0]
     */
    things.uniform.repartition(3)
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$DataPath/repartition_RR")
  }
}