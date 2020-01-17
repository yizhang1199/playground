package spark.test

import org.apache.spark.sql.{AnalysisException, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row
import org.apache.spark.SparkException

object SparkTestApp extends App {

  val spark = SparkSession.builder()
    .appName("RangePartitionApp")
    .master("local[4]")
    .getOrCreate()

  import spark.implicits._


  def createSchema() = {
    var schema = new StructType()
    schema
      .add("id", "string", true)
      .add("value", "string", true)
  }

  spark
    .udf.register("square", (param: String) => {
    if (param == "value2") {
      throw new Exception("failed")
    }
    param
  })

  val someData = Seq(
    Row("id1", "value1"),
    Row("id2", "value2")
  )

  val df = spark.createDataFrame(
    spark.sparkContext.parallelize(someData),
    createSchema()
  )
  df.createOrReplaceTempView("my_temporal_table")

  spark.sql("SELECT id, square(value) FROM my_temporal_table LIMIT 1").toJSON.explain(true)

  println("=======================================================================================")
  spark.sql("SELECT id, square(value) FROM my_temporal_table LIMIT 1").explain(true)
}
