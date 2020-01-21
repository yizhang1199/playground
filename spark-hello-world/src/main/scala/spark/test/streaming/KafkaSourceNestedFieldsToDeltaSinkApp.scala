package spark.test.streaming

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StringType
import spark.test.SparkHelper
import spark.test.SparkHelper.{corruptRecordOptions, userSchema}

import scala.concurrent.duration._

/**
 * See KafkaSourceToDeltaSinkApp for other Delta behaviors
 *
 * Exception in thread "main" org.apache.spark.sql.streaming.StreamingQueryException: Nested field is not supported in the INSERT clause of MERGE operation (field = `name`.`first`).;
 * === Streaming Query ===
 * Identifier: KafkaSourceWithNestedFieldsToDeltaSinkApp$ [id = c9521462-39a2-48ae-8263-bc16eaad365c, runId = 2d2b2646-fb79-47e4-862c-185d8b845f4a]
 * Current Committed Offsets: {}
 * Current Available Offsets: {KafkaV2[Subscribe[UsersTopic]]: {"UsersTopic":{"0":49}}}
 *
 * Current State: ACTIVE
 * Thread State: RUNNABLE
 *
 * Logical Plan:
 * Project [userId#732, login#733, name#734, _corrupt_record#735]
 * +- Project [data#730.userId AS userId#732, data#730.login AS login#733, data#730.name AS name#734, data#730._corrupt_record AS _corrupt_record#735, data#730]
 * +- Project [jsontostructs(StructField(userId,IntegerType,false), StructField(login,StringType,false), StructField(name,StructType(StructField(first,StringType,true), StructField(last,StringType,true)),true), StructField(_corrupt_record,StringType,true), (mode,PERMISSIVE), (columnNameOfCorruptRecord,_corrupt_record), value#728, Some(US/Pacific)) AS data#730]
 * +- Project [cast(value#715 as string) AS value#728]
 * +- StreamingExecutionRelation KafkaV2[Subscribe[UsersTopic]], [key#714, value#715, topic#716, partition#717, offset#718L, timestamp#719, timestampType#720]
 */
object KafkaSourceNestedFieldsToDeltaSinkApp extends App {
  private val name: String = KafkaSourceNestedFieldsToDeltaSinkApp.getClass.getSimpleName

  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  import spark.implicits._

  val sinkPath = StreamingSink.sinkPath(name, "delta") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  // create the delta table path otherwise streaming fails with "..." is not a Delta table
  // TODO what do we do in PROD with live streams?
  val initialUsersDf = SparkHelper.readJson(filename = "userInit.json", schema = userSchema)
  println("===============initialUsersDf===============")
  initialUsersDf.printSchema()
  initialUsersDf
    .write
    .format("delta")
    .mode(SaveMode.Overwrite)
    .save(sinkPath)

  /**
   * Read from Kafka source, which has a fixed schema
   */
  val streamingDF = spark // TODO from_json does not work with columnNameOfCorruptRecord -- bad rows are dropped
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "UsersTopic")
    .load()
    .select($"value".cast(StringType))
    .select(from_json($"value", userSchema, corruptRecordOptions).as("data"))
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

  streamingDF
    .writeStream
    .queryName(name)
    .trigger(Trigger.ProcessingTime(1.seconds)) // Configure for a 1-second micro-batch
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("delta") // Specify the sink type, a Parquet file
    .option("checkpointLocation", checkpointPath) // Specify the location of checkpoint files & W-A logs
    .foreachBatch(upsert _)
    .start()
    .awaitTermination

  /**
   * Exception in thread "main" org.apache.spark.sql.streaming.StreamingQueryException: Nested field is not supported in the INSERT clause of MERGE operation (field = `name`.`first`).;
   * === Streaming Query ===
   * Identifier: KafkaSourceWithNestedFieldsToDeltaSinkApp$ [id = c9521462-39a2-48ae-8263-bc16eaad365c, runId = 2d2b2646-fb79-47e4-862c-185d8b845f4a]
   * Current Committed Offsets: {}
   * Current Available Offsets: {KafkaV2[Subscribe[UsersTopic]]: {"UsersTopic":{"0":49}}}
   *
   * Current State: ACTIVE
   * Thread State: RUNNABLE
   *
   * Logical Plan:
   * Project [userId#732, login#733, name#734, _corrupt_record#735]
   * +- Project [data#730.userId AS userId#732, data#730.login AS login#733, data#730.name AS name#734, data#730._corrupt_record AS _corrupt_record#735, data#730]
   * +- Project [jsontostructs(StructField(userId,IntegerType,false), StructField(login,StringType,false), StructField(name,StructType(StructField(first,StringType,true), StructField(last,StringType,true)),true), StructField(_corrupt_record,StringType,true), (mode,PERMISSIVE), (columnNameOfCorruptRecord,_corrupt_record), value#728, Some(US/Pacific)) AS data#730]
   * +- Project [cast(value#715 as string) AS value#728]
   * +- StreamingExecutionRelation KafkaV2[Subscribe[UsersTopic]], [key#714, value#715, topic#716, partition#717, offset#718L, timestamp#719, timestampType#720]
   *
   * at org.apache.spark.sql.execution.streaming.StreamExecution.org$apache$spark$sql$execution$streaming$StreamExecution$$runStream(StreamExecution.scala:302)
   * at org.apache.spark.sql.execution.streaming.StreamExecution$$anon$1.run(StreamExecution.scala:193)
   * Caused by: org.apache.spark.sql.AnalysisException: Nested field is not supported in the INSERT clause of MERGE operation (field = `name`.`first`).;
   * at org.apache.spark.sql.delta.DeltaErrors$.nestedFieldNotSupported(DeltaErrors.scala:471)
   * at org.apache.spark.sql.delta.PreprocessTableMerge.$anonfun$apply$8(PreprocessTableMerge.scala:78)
   * at org.apache.spark.sql.delta.PreprocessTableMerge.$anonfun$apply$8$adapted(PreprocessTableMerge.scala:74)
   * at scala.collection.Iterator.foreach(Iterator.scala:941)
   * at scala.collection.Iterator.foreach$(Iterator.scala:941)
   * at scala.collection.AbstractIterator.foreach(Iterator.scala:1429)
   * at scala.collection.IterableLike.foreach(IterableLike.scala:74)
   * at scala.collection.IterableLike.foreach$(IterableLike.scala:73)
   * at scala.collection.AbstractIterable.foreach(Iterable.scala:56)
   * at org.apache.spark.sql.delta.PreprocessTableMerge.$anonfun$apply$7(PreprocessTableMerge.scala:74)
   * at scala.Option.map(Option.scala:163)
   * at org.apache.spark.sql.delta.PreprocessTableMerge.apply(PreprocessTableMerge.scala:72)
   * at io.delta.tables.DeltaMergeBuilder.execute(DeltaMergeBuilder.scala:233)
   * at spark.test.streaming.KafkaSourceWithNestedFieldsToDeltaSinkApp$.upsert(KafkaSourceWithNestedFieldsToDeltaSinkApp.scala:181)
   * at spark.test.streaming.KafkaSourceWithNestedFieldsToDeltaSinkApp$.$anonfun$new$1(KafkaSourceWithNestedFieldsToDeltaSinkApp.scala:157)
   * at spark.test.streaming.KafkaSourceWithNestedFieldsToDeltaSinkApp$.$anonfun$new$1$adapted(KafkaSourceWithNestedFieldsToDeltaSinkApp.scala:157)
   * at org.apache.spark.sql.execution.streaming.sources.ForeachBatchSink.addBatch(ForeachBatchSink.scala:35)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runBatch$15(MicroBatchExecution.scala:537)
   * at org.apache.spark.sql.execution.SQLExecution$.$anonfun$withNewExecutionId$1(SQLExecution.scala:78)
   * at org.apache.spark.sql.execution.SQLExecution$.withSQLConfPropagated(SQLExecution.scala:125)
   * at org.apache.spark.sql.execution.SQLExecution$.withNewExecutionId(SQLExecution.scala:73)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runBatch$14(MicroBatchExecution.scala:536)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken(ProgressReporter.scala:351)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken$(ProgressReporter.scala:349)
   * at org.apache.spark.sql.execution.streaming.StreamExecution.reportTimeTaken(StreamExecution.scala:58)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.runBatch(MicroBatchExecution.scala:535)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runActivatedStream$2(MicroBatchExecution.scala:198)
   * at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken(ProgressReporter.scala:351)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken$(ProgressReporter.scala:349)
   * at org.apache.spark.sql.execution.streaming.StreamExecution.reportTimeTaken(StreamExecution.scala:58)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runActivatedStream$1(MicroBatchExecution.scala:166)
   * at org.apache.spark.sql.execution.streaming.ProcessingTimeExecutor.execute(TriggerExecutor.scala:56)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.runActivatedStream(MicroBatchExecution.scala:160)
   * at org.apache.spark.sql.execution.streaming.StreamExecution.org$apache$spark$sql$execution$streaming$StreamExecution$$runStream(StreamExecution.scala:281)
   * ... 1 more
   */
  def upsert(microBatchDF: DataFrame, batchId: Long): Unit = {
    // https://docs.databricks.com/delta/delta-update.html#upsert-into-a-table-using-merge
    // API doc: https://docs.delta.io/0.5.0/api/scala/io/delta/tables/index.html
    DeltaTable.forPath(spark, sinkPath)
      .as("events")
      .merge(
        microBatchDF.as("updates"),
        "events.userId = updates.userId")
      .whenMatched
      .updateExpr(
        Map(
          "login" -> "updates.login",
          "name.first" -> "updates.name.first",
          "name.last" -> "updates.name.last"))
      .whenNotMatched
      .insertExpr(
        Map(
          "userId" -> "updates.userId",
          "login" -> "updates.login",
          "name.first" -> "updates.name.first",
          "name.last" -> "updates.name.last"))
      .execute()
  }

  /**
   * Exception in thread "main" org.apache.spark.sql.streaming.StreamingQueryException: cannot resolve `*` in UPDATE clause given columns events.`login`, events.`name`, events.`userId`;
   * === Streaming Query ===
   * Identifier: KafkaSourceNestedFieldsToDeltaSinkApp$ [id = 886cbc99-1292-4f47-a54c-bb0c6e90e2ed, runId = 9fbd22c9-6201-4b37-85c8-db8196152bff]
   * Current Committed Offsets: {}
   * Current Available Offsets: {KafkaV2[Subscribe[UsersTopic]]: {"UsersTopic":{"0":49}}}
   *
   * Current State: ACTIVE
   * Thread State: RUNNABLE
   *
   * Logical Plan:
   * Project [userId#732, login#733, name#734, _corrupt_record#735]
   * +- Project [data#730.userId AS userId#732, data#730.login AS login#733, data#730.name AS name#734, data#730._corrupt_record AS _corrupt_record#735, data#730]
   * +- Project [jsontostructs(StructField(userId,IntegerType,false), StructField(login,StringType,false), StructField(name,StructType(StructField(first,StringType,true), StructField(last,StringType,true)),true), StructField(_corrupt_record,StringType,true), (mode,PERMISSIVE), (columnNameOfCorruptRecord,_corrupt_record), value#728, Some(US/Pacific)) AS data#730]
   * +- Project [cast(value#715 as string) AS value#728]
   * +- StreamingExecutionRelation KafkaV2[Subscribe[UsersTopic]], [key#714, value#715, topic#716, partition#717, offset#718L, timestamp#719, timestampType#720]
   *
   * at org.apache.spark.sql.execution.streaming.StreamExecution.org$apache$spark$sql$execution$streaming$StreamExecution$$runStream(StreamExecution.scala:302)
   * at org.apache.spark.sql.execution.streaming.StreamExecution$$anon$1.run(StreamExecution.scala:193)
   * Caused by: org.apache.spark.sql.AnalysisException: cannot resolve `*` in UPDATE clause given columns events.`login`, events.`name`, events.`userId`;
   * at org.apache.spark.sql.catalyst.analysis.package$AnalysisErrorAt.failAnalysis(package.scala:42)
   * at org.apache.spark.sql.catalyst.plans.logical.MergeInto$.$anonfun$resolveReferences$3(merge.scala:248)
   * at scala.collection.mutable.ResizableArray.foreach(ResizableArray.scala:62)
   * at scala.collection.mutable.ResizableArray.foreach$(ResizableArray.scala:55)
   * at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:49)
   * at org.apache.spark.sql.catalyst.plans.logical.MergeInto$.resolveOrFail$1(merge.scala:244)
   * at org.apache.spark.sql.catalyst.plans.logical.MergeInto$.$anonfun$resolveReferences$5(merge.scala:287)
   * at scala.collection.TraversableLike.$anonfun$flatMap$1(TraversableLike.scala:244)
   * at scala.collection.Iterator.foreach(Iterator.scala:941)
   * at scala.collection.Iterator.foreach$(Iterator.scala:941)
   * at scala.collection.AbstractIterator.foreach(Iterator.scala:1429)
   * at scala.collection.IterableLike.foreach(IterableLike.scala:74)
   * at scala.collection.IterableLike.foreach$(IterableLike.scala:73)
   * at scala.collection.AbstractIterable.foreach(Iterable.scala:56)
   * at scala.collection.TraversableLike.flatMap(TraversableLike.scala:244)
   * at scala.collection.TraversableLike.flatMap$(TraversableLike.scala:241)
   * at scala.collection.AbstractTraversable.flatMap(Traversable.scala:108)
   * at org.apache.spark.sql.catalyst.plans.logical.MergeInto$.resolveClause$1(merge.scala:259)
   * at org.apache.spark.sql.catalyst.plans.logical.MergeInto$.$anonfun$resolveReferences$10(merge.scala:306)
   * at scala.collection.TraversableLike.$anonfun$map$1(TraversableLike.scala:237)
   * at scala.collection.immutable.List.foreach(List.scala:392)
   * at scala.collection.TraversableLike.map(TraversableLike.scala:237)
   * at scala.collection.TraversableLike.map$(TraversableLike.scala:230)
   * at scala.collection.immutable.List.map(List.scala:298)
   * at org.apache.spark.sql.catalyst.plans.logical.MergeInto$.resolveReferences(merge.scala:305)
   * at io.delta.tables.DeltaMergeBuilder.execute(DeltaMergeBuilder.scala:228)
   * at spark.test.streaming.KafkaSourceNestedFieldsToDeltaSinkApp$.upsert(KafkaSourceNestedFieldsToDeltaSinkApp.scala:176)
   * at spark.test.streaming.KafkaSourceNestedFieldsToDeltaSinkApp$.$anonfun$new$1(KafkaSourceNestedFieldsToDeltaSinkApp.scala:157)
   * at spark.test.streaming.KafkaSourceNestedFieldsToDeltaSinkApp$.$anonfun$new$1$adapted(KafkaSourceNestedFieldsToDeltaSinkApp.scala:157)
   * at org.apache.spark.sql.execution.streaming.sources.ForeachBatchSink.addBatch(ForeachBatchSink.scala:35)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runBatch$15(MicroBatchExecution.scala:537)
   * at org.apache.spark.sql.execution.SQLExecution$.$anonfun$withNewExecutionId$1(SQLExecution.scala:78)
   * at org.apache.spark.sql.execution.SQLExecution$.withSQLConfPropagated(SQLExecution.scala:125)
   * at org.apache.spark.sql.execution.SQLExecution$.withNewExecutionId(SQLExecution.scala:73)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runBatch$14(MicroBatchExecution.scala:536)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken(ProgressReporter.scala:351)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken$(ProgressReporter.scala:349)
   * at org.apache.spark.sql.execution.streaming.StreamExecution.reportTimeTaken(StreamExecution.scala:58)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.runBatch(MicroBatchExecution.scala:535)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runActivatedStream$2(MicroBatchExecution.scala:198)
   * at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken(ProgressReporter.scala:351)
   * at org.apache.spark.sql.execution.streaming.ProgressReporter.reportTimeTaken$(ProgressReporter.scala:349)
   * at org.apache.spark.sql.execution.streaming.StreamExecution.reportTimeTaken(StreamExecution.scala:58)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.$anonfun$runActivatedStream$1(MicroBatchExecution.scala:166)
   * at org.apache.spark.sql.execution.streaming.ProcessingTimeExecutor.execute(TriggerExecutor.scala:56)
   * at org.apache.spark.sql.execution.streaming.MicroBatchExecution.runActivatedStream(MicroBatchExecution.scala:160)
   * at org.apache.spark.sql.execution.streaming.StreamExecution.org$apache$spark$sql$execution$streaming$StreamExecution$$runStream(StreamExecution.scala:281)
   * ... 1 more
   */
  def upsert2(microBatchDF: DataFrame, batchId: Long): Unit = {
    // https://docs.databricks.com/delta/delta-update.html#upsert-into-a-table-using-merge
    // API doc: https://docs.delta.io/0.5.0/api/scala/io/delta/tables/index.html
    DeltaTable.forPath(spark, sinkPath)
      .as("events")
      .merge(
        microBatchDF.as("updates"),
        "events.userId = updates.userId")
      .whenMatched
      .updateExpr(
        Map(
          "*" -> "updates.*"))
      .whenNotMatched
      .insertExpr(
        Map(
          "*" -> "updates.*"))
      .execute()
  }
}
