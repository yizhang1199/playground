package spark.test

import org.apache.spark.sql.SparkSession

/**
 *
 */
object AdHocApp extends App {
  private val name: String = AdHocApp.getClass.getSimpleName

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  val df = spark.read
      .format("delta")
      .load("/Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/streaming-sinks/KafkaSourceNestedFieldsToDeltaSinkApp$/output.delta")

  df.printSchema()
  df.sort("userId").show(20, truncate = false)
}
