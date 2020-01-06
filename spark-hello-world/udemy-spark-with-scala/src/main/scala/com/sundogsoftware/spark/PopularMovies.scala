package com.sundogsoftware.spark

import java.nio.charset.{Charset, CodingErrorAction}

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions._

import scala.io.{Codec, Source}

object PopularMovies {

  def main(args: Array[String]) {

    val sparkSession = SparkSession.builder()
      .appName("MaxTemperatures")
      .master("local[4]")
      .getOrCreate()

    /**
     * user_id movie_id rating timestamp
     * 196	242	3	881250949
     * 186	302	3	891717742
     * 22	377	1	878887116
     * 244	51	2	880606923
     * 166	346	1	886397596
     * 298	474	4	884182806
     * 115	265	2	881171488
     * 253	465	5	891628467
     * 305	451	3	886324817
     * 6	86	3	883603013
     */
    val df = sparkSession.read
      .text(this.getClass.getClassLoader.getResource("ml-100k/u.data").getFile)

    import sparkSession.implicits._

    val movieNamesDictionary = sparkSession.sparkContext.broadcast(loadMovieNames)

    val filteredDf = df.rdd.map {
      case Row(value: String) => value.split("\\s")(1) // get movie_id
    }.toDF().withColumnRenamed("value", "movie_id")

    filteredDf.printSchema()
    filteredDf.show(20)

    val lookupUdf = udf { movieId: Int => movieNamesDictionary.value(movieId) }

    val resultDf = filteredDf
      .groupBy($"movie_id")
      .agg(
        count(lit(1)).alias("count")
      ).sort($"count".desc).withColumn("movie_id", lookupUdf($"movie_id"))

    resultDf.show(20)
  }

  private val MovieNameRegex = """\s*(\d+)\|([^|]+).*""".r
  private def loadMovieNames: Map[Int, String] = {
    implicit val codec = Codec(Charset forName "UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    val lines = Source.fromResource("ml-100k/u.item").getLines()

    lines.collect {
      case MovieNameRegex(movieId, movieName) =>
        movieId.toInt -> movieName
    }.toMap
  }
}
