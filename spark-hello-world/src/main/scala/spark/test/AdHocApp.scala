package spark.test

import org.apache.spark.sql.SparkSession

/**
 *
 */
object AdHocApp extends App {
  private val name: String = AdHocApp.getClass.getSimpleName

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  val df = spark.read
      .format("parquet")
      .load("/Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/partition-test/repartitionByRange_skewedAgeSalt")

  df.printSchema()
  println(s"Total count=${df.count()}")
  df.groupBy("age").count().sort("age").show(truncate = false, numRows = 200)
}
