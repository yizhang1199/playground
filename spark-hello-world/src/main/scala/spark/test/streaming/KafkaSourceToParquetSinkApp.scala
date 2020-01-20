package spark.test.streaming

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType
import spark.test.SparkHelper

/**
 * How to deserialize keys and values from Kafka streams: when using Spark, keys and values are always deserialized
 * as byte arrays with ByteArrayDeserializer. Use DataFrame operations to explicitly deserialize the values.
 *
 * https://docs.databricks.com/spark/latest/structured-streaming/kafka.html
 * https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html
 *
 * from_json returns wrong result if corrupt record column is in the middle of schema
 * https://issues.apache.org/jira/browse/SPARK-25952
 */
object KafkaSourceToParquetSinkApp extends App {
  private val name: String = KafkaSourceToParquetSinkApp.getClass.getSimpleName

  val spark = SparkHelper.initSpark(name)

  import spark.implicits._

  /**
   * Kafka source has a fixed schema and cannot be set with a custom one:
   *
   * * root
   * * |-- key: binary (nullable = true)
   * * |-- value: binary (nullable = true)
   * * |-- topic: string (nullable = true)
   * * |-- partition: integer (nullable = true)
   * * |-- offset: long (nullable = true)
   * * |-- timestamp: timestamp (nullable = true)
   * * |-- timestampType: integer (nullable = true)
   */
  val streamingDF = spark // TODO from_json does not work with columnNameOfCorruptRecord -- bad rows are dropped
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "MyTopic")
    .load()
    .select($"value".cast(StringType))
    .select(from_json($"value", SparkHelper.userSchema, SparkHelper.corruptRecordOptions).as("data"))
    //.select(to_json($"data", jsonOptions).as("json"))
    .select("data.*", "*") // TODO is this the best way to explode a StructType column?
    .drop("data")

  /**
   * root
   * |-- data: struct (nullable = true)
   * |    |-- userId: integer (nullable = true)
   * |    |-- login: string (nullable = true)
   * |    |-- name: struct (nullable = true)
   * |    |    |-- first: string (nullable = true)
   * |    |    |-- last: string (nullable = true)
   * |    |-- _corrupt_record: string (nullable = true)
   */
  println("====================================")
  streamingDF.printSchema()

  StreamingSink.writeToParquetSink(name, streamingDF)
}
