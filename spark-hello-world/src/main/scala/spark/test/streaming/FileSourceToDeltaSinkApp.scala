package spark.test.streaming

import io.delta.tables.DeltaTable
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.streaming.Trigger
import spark.test.streaming.StreamingSource.jsonSourcePath
import spark.test.SparkHelper
import spark.test.SparkHelper.userSchema

import scala.concurrent.duration._
/**
 * https://docs.databricks.com/delta/delta-streaming.html
 *
 * Malformed data will cause the entire job to abort with this exception:
 * Exception in thread "main" org.apache.spark.sql.streaming.StreamingQueryException: Job aborted due to stage failure: Task 0 in stage 14.0 failed 1 times, most recent failure: Lost task 0.0 in stage 14.0 (TID 255, localhost, executor driver): java.lang.RuntimeException: The 0th field 'userId' of input row cannot be null.
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.serializefromobject_doConsume_0$(generated.java:177)
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.agg_doAggregateWithKeys_0$(generated.java:363)
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.processNext(generated.java:376)
 * at org.apache.spark.sql.execution.BufferedRowIterator.hasNext(BufferedRowIterator.java:43)
 * at org.apache.spark.sql.execution.WholeStageCodegenExec$$anon$2.hasNext(WholeStageCodegenExec.scala:636)
 * at scala.collection.Iterator$$anon$10.hasNext(Iterator.scala:458)
 * at org.apache.spark.shuffle.sort.BypassMergeSortShuffleWriter.write(BypassMergeSortShuffleWriter.java:125)
 * at org.apache.spark.scheduler.ShuffleMapTask.runTask(ShuffleMapTask.scala:99)
 * at org.apache.spark.scheduler.ShuffleMapTask.runTask(ShuffleMapTask.scala:55)
 * at org.apache.spark.scheduler.Task.run(Task.scala:123)
 * at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$3(Executor.scala:411)
 * at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1360)
 * at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:414)
 * at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
 * at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
 * at java.lang.Thread.run(Thread.java:748)
 *
 * Driver stacktrace:
 * === Streaming Query ===
 * Identifier: FileSourceToDeltaSinkApp$ [id = 5bb010f9-95be-442f-b7e2-de5bf54bb20b, runId = db16ad06-f541-4f38-9000-784361966499]
 * Current Committed Offsets: {}
 * Current Available Offsets: {FileStreamSource[file:/Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/streaming-sources/json]: {"logOffset":0}}
 *
 * Current State: ACTIVE
 * Thread State: RUNNABLE
 *
 * Logical Plan:
 * Project [userId#714, login#715, name#716.first AS name_first#722, name#716.last AS name_last#723]
 * +- StreamingExecutionRelation FileStreamSource[file:/Users/yzhang/github/yizhang1199/playground/spark-hello-world/target/streaming-sources/json], [userId#714, login#715, name#716, _corrupt_record#717]
 *
 * at org.apache.spark.sql.execution.streaming.StreamExecution.org$apache$spark$sql$execution$streaming$StreamExecution$$runStream(StreamExecution.scala:302)
 * at org.apache.spark.sql.execution.streaming.StreamExecution$$anon$1.run(StreamExecution.scala:193)
 * Caused by: org.apache.spark.SparkException: Job aborted due to stage failure: Task 0 in stage 14.0 failed 1 times, most recent failure: Lost task 0.0 in stage 14.0 (TID 255, localhost, executor driver): java.lang.RuntimeException: The 0th field 'userId' of input row cannot be null.
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.serializefromobject_doConsume_0$(generated.java:177)
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.agg_doAggregateWithKeys_0$(generated.java:363)
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.processNext(generated.java:376)
 * at org.apache.spark.sql.execution.BufferedRowIterator.hasNext(BufferedRowIterator.java:43)
 * at org.apache.spark.sql.execution.WholeStageCodegenExec$$anon$2.hasNext(WholeStageCodegenExec.scala:636)
 * at scala.collection.Iterator$$anon$10.hasNext(Iterator.scala:458)
 * at org.apache.spark.shuffle.sort.BypassMergeSortShuffleWriter.write(BypassMergeSortShuffleWriter.java:125)
 * at org.apache.spark.scheduler.ShuffleMapTask.runTask(ShuffleMapTask.scala:99)
 * at org.apache.spark.scheduler.ShuffleMapTask.runTask(ShuffleMapTask.scala:55)
 * at org.apache.spark.scheduler.Task.run(Task.scala:123)
 * at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$3(Executor.scala:411)
 * at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1360)
 * at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:414)
 * at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
 * at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
 * at java.lang.Thread.run(Thread.java:748)
 *
 * Driver stacktrace:
 * at org.apache.spark.scheduler.DAGScheduler.failJobAndIndependentStages(DAGScheduler.scala:1889)
 * at org.apache.spark.scheduler.DAGScheduler.$anonfun$abortStage$2(DAGScheduler.scala:1877)
 * at org.apache.spark.scheduler.DAGScheduler.$anonfun$abortStage$2$adapted(DAGScheduler.scala:1876)
 * at scala.collection.mutable.ResizableArray.foreach(ResizableArray.scala:62)
 * at scala.collection.mutable.ResizableArray.foreach$(ResizableArray.scala:55)
 * at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:49)
 * at org.apache.spark.scheduler.DAGScheduler.abortStage(DAGScheduler.scala:1876)
 * at org.apache.spark.scheduler.DAGScheduler.$anonfun$handleTaskSetFailed$1(DAGScheduler.scala:926)
 * at org.apache.spark.scheduler.DAGScheduler.$anonfun$handleTaskSetFailed$1$adapted(DAGScheduler.scala:926)
 * at scala.Option.foreach(Option.scala:274)
 * at org.apache.spark.scheduler.DAGScheduler.handleTaskSetFailed(DAGScheduler.scala:926)
 * at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.doOnReceive(DAGScheduler.scala:2110)
 * at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:2059)
 * at org.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:2048)
 * at org.apache.spark.util.EventLoop$$anon$1.run(EventLoop.scala:49)
 * at org.apache.spark.scheduler.DAGScheduler.runJob(DAGScheduler.scala:737)
 * at org.apache.spark.SparkContext.runJob(SparkContext.scala:2061)
 * at org.apache.spark.SparkContext.runJob(SparkContext.scala:2082)
 * at org.apache.spark.SparkContext.runJob(SparkContext.scala:2101)
 * at org.apache.spark.SparkContext.runJob(SparkContext.scala:2126)
 * at org.apache.spark.rdd.RDD.$anonfun$collect$1(RDD.scala:945)
 * at org.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:151)
 * at org.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:112)
 * at org.apache.spark.rdd.RDD.withScope(RDD.scala:363)
 * at org.apache.spark.rdd.RDD.collect(RDD.scala:944)
 * at org.apache.spark.sql.execution.SparkPlan.executeCollect(SparkPlan.scala:299)
 * at org.apache.spark.sql.Dataset.$anonfun$count$1(Dataset.scala:2836)
 * at org.apache.spark.sql.Dataset.$anonfun$count$1$adapted(Dataset.scala:2835)
 * at org.apache.spark.sql.Dataset.$anonfun$withAction$2(Dataset.scala:3370)
 * at org.apache.spark.sql.execution.SQLExecution$.$anonfun$withNewExecutionId$1(SQLExecution.scala:78)
 * at org.apache.spark.sql.execution.SQLExecution$.withSQLConfPropagated(SQLExecution.scala:125)
 * at org.apache.spark.sql.execution.SQLExecution$.withNewExecutionId(SQLExecution.scala:73)
 * at org.apache.spark.sql.Dataset.withAction(Dataset.scala:3370)
 * at org.apache.spark.sql.Dataset.count(Dataset.scala:2835)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.findTouchedFiles(MergeIntoCommand.scala:224)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.$anonfun$run$3(MergeIntoCommand.scala:132)
 * at com.databricks.spark.util.DatabricksLogging.recordOperation(DatabricksLogging.scala:77)
 * at com.databricks.spark.util.DatabricksLogging.recordOperation$(DatabricksLogging.scala:67)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.recordOperation(MergeIntoCommand.scala:89)
 * at org.apache.spark.sql.delta.metering.DeltaLogging.recordDeltaOperation(DeltaLogging.scala:103)
 * at org.apache.spark.sql.delta.metering.DeltaLogging.recordDeltaOperation$(DeltaLogging.scala:89)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.recordDeltaOperation(MergeIntoCommand.scala:89)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.$anonfun$run$2(MergeIntoCommand.scala:132)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.$anonfun$run$2$adapted(MergeIntoCommand.scala:125)
 * at org.apache.spark.sql.delta.DeltaLog.withNewTransaction(DeltaLog.scala:396)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.$anonfun$run$1(MergeIntoCommand.scala:125)
 * at com.databricks.spark.util.DatabricksLogging.recordOperation(DatabricksLogging.scala:77)
 * at com.databricks.spark.util.DatabricksLogging.recordOperation$(DatabricksLogging.scala:67)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.recordOperation(MergeIntoCommand.scala:89)
 * at org.apache.spark.sql.delta.metering.DeltaLogging.recordDeltaOperation(DeltaLogging.scala:103)
 * at org.apache.spark.sql.delta.metering.DeltaLogging.recordDeltaOperation$(DeltaLogging.scala:89)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.recordDeltaOperation(MergeIntoCommand.scala:89)
 * at org.apache.spark.sql.delta.commands.MergeIntoCommand.run(MergeIntoCommand.scala:124)
 * at io.delta.tables.DeltaMergeBuilder.execute(DeltaMergeBuilder.scala:235)
 * at spark.test.streaming.FileSourceToDeltaSinkApp$.upsert(FileSourceToDeltaSinkApp.scala:80)
 * at spark.test.streaming.FileSourceToDeltaSinkApp$.$anonfun$new$1(FileSourceToDeltaSinkApp.scala:56)
 * at spark.test.streaming.FileSourceToDeltaSinkApp$.$anonfun$new$1$adapted(FileSourceToDeltaSinkApp.scala:56)
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
 * Caused by: java.lang.RuntimeException: The 0th field 'userId' of input row cannot be null.
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.serializefromobject_doConsume_0$(generated.java:177)
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.agg_doAggregateWithKeys_0$(generated.java:363)
 * at org.apache.spark.sql.catalyst.expressions.GeneratedClass$GeneratedIteratorForCodegenStage2.processNext(generated.java:376)
 * at org.apache.spark.sql.execution.BufferedRowIterator.hasNext(BufferedRowIterator.java:43)
 * at org.apache.spark.sql.execution.WholeStageCodegenExec$$anon$2.hasNext(WholeStageCodegenExec.scala:636)
 * at scala.collection.Iterator$$anon$10.hasNext(Iterator.scala:458)
 * at org.apache.spark.shuffle.sort.BypassMergeSortShuffleWriter.write(BypassMergeSortShuffleWriter.java:125)
 * at org.apache.spark.scheduler.ShuffleMapTask.runTask(ShuffleMapTask.scala:99)
 * at org.apache.spark.scheduler.ShuffleMapTask.runTask(ShuffleMapTask.scala:55)
 * at org.apache.spark.scheduler.Task.run(Task.scala:123)
 * at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$3(Executor.scala:411)
 * at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1360)
 * at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:414)
 * at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
 * at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
 * at java.lang.Thread.run(Thread.java:748)
 *
 */
object FileSourceToDeltaSinkApp extends App {
  private val name: String = FileSourceToDeltaSinkApp.getClass.getSimpleName
  implicit val spark: SparkSession = SparkHelper.initSpark(name)

  import spark.implicits._

  val sinkPath = StreamingSink.sinkPath(name, "delta") // A subdirectory for our output
  val checkpointPath = StreamingSink.checkpointPath(name) // A subdirectory for our checkpoint & W-A logs

  // TODO what do we do in PROD with live streams?
  // create the delta table path otherwise streaming fails with "..." is not a Delta table
  val initialUsersDf = SparkHelper.readJson(filename = "userInit.json", schema = userSchema)
    .select($"userId", $"login", $"name.first".as("name_first"), $"name.last".as("name_last"))
    .write
    .format("delta")
    .mode(SaveMode.Overwrite)
    .save(sinkPath)

  val streamingDF = spark
    .readStream
    .option("maxFilesPerTrigger", 1)
    .options(SparkHelper.corruptRecordOptions)
    .schema(SparkHelper.userSchema)
    .json(jsonSourcePath)

  streamingDF.printSchema()

  streamingDF
    .select($"userId", $"login", $"name.first".as("name_first"), $"name.last".as("name_last"))
    .writeStream
    .queryName(name)
    .trigger(Trigger.ProcessingTime(1.seconds))
    //.trigger(Trigger.Continuous(1.second)) // java.lang.IllegalStateException: Unknown type of trigger: ContinuousTrigger(1000)
    .format("delta")
    .option("checkpointLocation", checkpointPath) // Specify the location of checkpoint files & W-A logs
    .foreachBatch(upsert _)
    .start()
    .awaitTermination

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
          "name_first" -> "updates.name_first",
          "name_last" -> "updates.name_last"))
      .whenNotMatched
      .insertExpr(
        Map(
          "userId" -> "updates.userId",
          "login" -> "updates.login",
          "name_first" -> "updates.name_first",
          "name_last" -> "updates.name_last"))
      .execute()
  }
}
