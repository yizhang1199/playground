package com.sundogsoftware.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

object MaxTemperatures {

  // stationId: String, unknonwn1: String, entryType: String, temperature: Float, unknonwn2: String,
  // unknonwn3: String, unknonwn4: String, unknonwn5: String
  private val csvSchema = StructType(
    List(
      StructField("station_id", StringType, true),
      StructField("unknown1", StringType, true),
      StructField("entry_type", StringType, true),
      StructField("temperature", FloatType, true),
      StructField("unknonwn2", StringType, true),
      StructField("unknonwn3", StringType, true),
      StructField("unknonwn4", StringType, true),
      StructField("unknonwn5", StringType, true)
    )
  )

  def main(args: Array[String]) {

    val sparkSession = SparkSession.builder()
      .appName("MaxTemperatures")
      .master("local[4]")
      .getOrCreate()

    // Read CSV file, with the following fields:
    //StationId, ???, EntryType, Temperature, ???, ???, ???
    val df =  sparkSession.read
      .option("sep", ",")
      .schema(csvSchema)
      .csv(this.getClass.getClassLoader.getResource("1800.csv").getFile)

    import sparkSession.implicits._

    val resultDf = df.filter($"entry_type" === "TMAX")
      .select($"station_id", $"temperature")
      .groupBy($"station_id")
      .max("temperature")

    println(resultDf.take(10) mkString "\n")
  }
}
