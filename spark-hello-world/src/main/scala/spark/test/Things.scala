package spark.test

import java.time.{Duration, LocalDate, Month}

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.udf

import scala.util.Random

object Things {

  private[test] def setup(spark: SparkSession): DataFrame = {
    import spark.implicits._

    val range = spark.range(1, 1201) // id: long

    val thingName = udf { id: Long => s"Thing $id" }

    val thingAge = udf { () => Random.nextInt(12) + 1 }

    val thingColor = udf { id: Long =>
      id % 6 match {
        case 0 => "red"
        case 1 => "yellow"
        case 2 => "blue"
        case 3 => "green"
        case 4 => "purple"
        case 5 => "turquoise"
      }
    }

    val createDate = udf { () =>
      val createDate = LocalDate.of(2019, Month.JANUARY, 1).plusDays(Random.nextInt(365))
      new java.sql.Date(createDate.toEpochDay * Duration.ofDays(1).toMillis)
    }

    val things = range.withColumn("name", thingName($"id"))
      .withColumn("age", thingAge()) // lit(Random.nextInt(120) + 1) is only called once, instead of per row
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
    things.printSchema()
    things.show(120)

    things
  }
}
