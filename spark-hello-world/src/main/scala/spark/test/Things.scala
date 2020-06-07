package spark.test

import java.time.{Duration, LocalDate, Month}

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.udf

import scala.util.Random

class Things(count: Int = 120, skewedCount: Int = 120)(implicit spark: SparkSession) {

  import spark.implicits._

  private val range = spark.range(1, count + 1) // id: long
  private val skewedRange = spark.range(1, skewedCount + 1) // id: long

  private val thingName = udf { id: Long => s"Thing $id" }

  private val thingAge = udf { () => Random.nextInt(12) + 1 }

  private val skewedAge = udf { () =>
    Random.nextInt(100) match {
      case x if x < 65 => 1 // 65%
      case x if x >= 65 && x < 75 => 2 // 10%
      case x if x >= 75 && x < 95 => 3 // 20%
      case x if x >= 95 && x < 99 => 4 // 4%
      case 99 => 5 // 1%
    }
  }

  private val thingColor = udf { id: Long =>
    id % 6 match {
      case 0 => "red"
      case 1 => "yellow"
      case 2 => "blue"
      case 3 => "green"
      case 4 => "purple"
      case 5 => "turquoise"
    }
  }

  private val createDate = udf { () =>
    val createDate = LocalDate.of(2019, Month.JANUARY, 1).plusDays(Random.nextInt(365))
    new java.sql.Date(createDate.toEpochDay * Duration.ofDays(1).toMillis)
  }

  val uniform: DataFrame = range
    .withColumn("name", thingName($"id"))
    .withColumn("age", thingAge()) // lit(Random.nextInt(120) + 1) is only called once, instead of per row
    .withColumn("color", thingColor($"id"))
    .withColumn("createDate", createDate())

  val skewed: DataFrame = skewedRange
    .withColumn("name", thingName($"id"))
    .withColumn("age", skewedAge())
    .withColumn("color", thingColor($"id"))
    .withColumn("createDate", createDate())

  /**
   * root
   * |-- id: long (nullable = false)
   * |-- name: string (nullable = true)
   * |-- age: integer (nullable = false)
   * |-- color: string (nullable = true)
   * |-- createDate: date (nullable = true)
   */
  // things.printSchema()
  // things.show(120)
}
