package spark.test

import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.reflect.io.{Directory, File}

/**
 * Predicate Pushdown for Nested fields is not complete on spark 2.4.4
 *
 * Example 1: No PushedFilters when filtering on array column that contains nested fields
 * spark.sql("select * from person where array_contains(children.age, 30)") -> PushedFilters: []
 * == Physical Plan ==
 * CollectLimit 21
 * +- *(1) Project [cast(address#0 as string) AS address#241, cast(age#1L as string) AS age#242, cast(children#2 as string) AS children#243, name#3]
 * +- *(1) Filter array_contains(children#2.age, 30)
 * +- *(1) FileScan parquet [address#0,age#1L,children#2,name#3] Batched: false, Format: Parquet, Location: InMemoryFileIndex[file:/Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/Neste..., PartitionFilters: [], PushedFilters: [], ReadSchema: struct<address:struct<city:string,country:string,street:string>,age:bigint,children:array<struct<...
 *
 * Example 2: compare PushedFilters for flat column vs nested (but not an array) column
 * spark.sql("select * from person where age>200") -> PushedFilters: [IsNotNull(age), GreaterThan(age,200)]
 * == Physical Plan ==
 * CollectLimit 21
 * +- *(1) Project [cast(address#0 as string) AS address#305, cast(age#1L as string) AS age#306, cast(children#2 as string) AS children#307, name#3]
 * +- *(1) Filter (isnotnull(age#1L) && (age#1L > 200))
 * +- *(1) FileScan parquet [address#0,age#1L,children#2,name#3] Batched: false, Format: Parquet, Location: InMemoryFileIndex[file:/Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/Neste..., PartitionFilters: [], PushedFilters: [IsNotNull(age), GreaterThan(age,200)], ReadSchema: struct<address:struct<city:string,country:string,street:string>,age:bigint,children:array<struct<...
 *
 * spark.sql("select * from person where address.city = 'Nada'") -> PushedFilters: [IsNotNull(address)]
 * == Physical Plan ==
 * CollectLimit 21
 * +- *(1) Project [cast(address#0 as string) AS address#349, cast(age#1L as string) AS age#350, cast(children#2 as string) AS children#351, name#3]
 * +- *(1) Filter (isnotnull(address#0) && (address#0.city = Nada))
 * +- *(1) FileScan parquet [address#0,age#1L,children#2,name#3] Batched: false, Format: Parquet, Location: InMemoryFileIndex[file:/Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/Neste..., PartitionFilters: [], PushedFilters: [IsNotNull(address)], ReadSchema: struct<address:struct<city:string,country:string,street:string>,age:bigint,children:array<struct<...
 *
 * More info:
 * https://stackoverflow.com/questions/57331007/efficient-reading-nested-parquet-column-in-spark
 * https://issues.apache.org/jira/browse/SPARK-4502
 * https://issues.apache.org/jira/browse/SPARK-25556 (Predicate Pushdown for Nested fields)
 */
object NestedFieldsApp extends App {
  private val name: String = NestedFieldsApp.getClass.getSimpleName

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  val nestedJsonDF = SparkHelper.readJson(filename = "personsMultiLine.json", multiLine = true)

  /**
   * root
   * |-- address: struct (nullable = true)
   * |    |-- city: string (nullable = true)
   * |    |-- street: string (nullable = true)
   * |-- children: array (nullable = true)
   * |    |-- element: struct (containsNull = true)
   * |    |    |-- age: long (nullable = true)
   * |    |    |-- birthdate: string (nullable = true)
   * |    |    |-- name: string (nullable = true)
   * |-- name: string (nullable = true)
   */
  nestedJsonDF.printSchema()

  val parquetPath = writeNestedJsonToParque(nestedJsonDF)

  val readFromParquet = spark
    .read
    .parquet(parquetPath)

  readFromParquet.createOrReplaceTempView("person")
  readFromParquet.sqlContext.sql("select * from person").show(20)

  //----------------------------------------------------------
  // Helper methods
  //----------------------------------------------------------
  private def writeNestedJsonToParque(nestedJsonDF: DataFrame): String = {
    val parquetPath = s"target/$name/parquet"
    val dir = Directory(File(parquetPath))
    dir.deleteRecursively()

    /**
     * 20/01/19 15:00:47 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
     * {
     * "type" : "struct",
     * "fields" : [ {
     * "name" : "address",
     * "type" : {
     * "type" : "struct",
     * "fields" : [ {
     * "name" : "city",
     * "type" : "string",
     * "nullable" : true,
     * "metadata" : { }
     * }, {
     * "name" : "street",
     * "type" : "string",
     * "nullable" : true,
     * "metadata" : { }
     * } ]
     * },
     * "nullable" : true,
     * "metadata" : { }
     * }, {
     * "name" : "children",
     * "type" : {
     * "type" : "array",
     * "elementType" : {
     * "type" : "struct",
     * "fields" : [ {
     * "name" : "age",
     * "type" : "long",
     * "nullable" : true,
     * "metadata" : { }
     * }, {
     * "name" : "birthdate",
     * "type" : "string",
     * "nullable" : true,
     * "metadata" : { }
     * }, {
     * "name" : "name",
     * "type" : "string",
     * "nullable" : true,
     * "metadata" : { }
     * } ]
     * },
     * "containsNull" : true
     * },
     * "nullable" : true,
     * "metadata" : { }
     * }, {
     * "name" : "name",
     * "type" : "string",
     * "nullable" : true,
     * "metadata" : { }
     * } ]
     * }
     * and corresponding Parquet message type:
     * message spark_schema {
     * optional group address {
     * optional binary city (UTF8);
     * optional binary street (UTF8);
     * }
     * optional group children (LIST) {
     * repeated group list {
     * optional group element {
     * optional int64 age;
     * optional binary birthdate (UTF8);
     * optional binary name (UTF8);
     * }
     * }
     * }
     * optional binary name (UTF8);
     * }
     */
    nestedJsonDF
      .write
      .format("parquet") // Specify the sink type, a Parquet file
      .parquet(parquetPath)

    parquetPath
  }
}
