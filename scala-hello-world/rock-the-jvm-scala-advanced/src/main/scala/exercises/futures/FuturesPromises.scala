package exercises.futures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Random, Try}

/**
 * Future is a Monad, can use map, flatMap, filter and for-comprehension
 */
object FuturesPromises extends App {

  /**
   * Basics
   */
  def log(message: String, sleepMillis: Duration = 100.milliseconds): Unit = {
    Thread.sleep(sleepMillis.toMillis)
    if (message == "error") {
      println(s"error: $message")
      throw new RuntimeException(s"failed with $message")
    } else {
      println(s"success: $message")
    }
  }

  val logFutureWithException = Future[Unit](log("error"))
  val logFutureSuccess = Future[Unit](log("Hello World!"))

  println(s"Before Await: logFutureSuccess.value: ${logFutureSuccess.value}")
  println(s"Before Await: logFutureWithException.value: ${logFutureWithException.value}")

  val f1: Future[Unit] = Await.ready(logFutureWithException, 500.milliseconds)
  val f2: Future[Unit] = Await.ready(logFutureSuccess, 500.milliseconds)

  println(s"After Await: logFutureSuccess.value: ${logFutureSuccess.value}")
  println(s"After Await: logFutureWithException.value: ${logFutureWithException.value}")

  /**
   * Exercise 1: fullfill a future immediately with a value
   */
  Thread.sleep(100)
  println("\n----- Exercise #1 -----")

  def fullfillImmediately[T](value: T): Future[T] = Future {
    println(s"fullfillImmediately: $value")
    value
  } // value has already been computed

  fullfillImmediately("Hello World!").value

  /**
   * Exercise 2: inSequence
   */
  Thread.sleep(100)
  println("\n----- Exercise #2 -----")

  // creates a new future that runs futureA first before running futureB
  // Note that the new future != futureB and will not affect when futureB runs.
  def inSequence[A, B](futureA: Future[A], futureB: Future[B]): Future[B] = {
    futureA.flatMap(_ => futureB)
  }

  def double(number: String, sleepMillis: Duration = 1.milliseconds): Int = {
    Thread.sleep(sleepMillis.toMillis)
    println(s"double $number")
    number.toInt * 2
  }

  private val logFuture = Future(log("call log", 200.milliseconds))
  private val doubleFuture = Future(double("blah"))

  Await.ready(doubleFuture, 250.milliseconds)
  Await.ready(logFuture, 250.milliseconds)

  println(s"doubleFuture.value: ${doubleFuture.value}")
  println(s"logFuture.value: ${logFuture.value}")

  // Note that chainedFuture does NOT impact when doubleFuture or logFuture will execute.
  val chainedFuture = inSequence[Unit, Int](logFuture, doubleFuture)

  Await.ready(logFuture, 250.milliseconds)

  println(s"doubleFuture.value: ${doubleFuture.value}")
  println(s"logFuture.value: ${logFuture.value}")
  println(s"chainedFuture.value: ${chainedFuture.value}")

  /**
   * Exercise 3: first out of 2 futures
   */
  Thread.sleep(100)
  println("\n----- Exercise #3: get the future that completed first -----")

  // returns the first future that completes
  def first[A](futureA: Future[A], futureB: Future[A]): Future[A] = {
    val promise: Promise[A] = Promise[A]

    futureA.onComplete(promise.tryComplete)
    futureB.onComplete(promise.tryComplete)

    promise.future
  }

  val fast = Future(double("3"))
  val slow = Future(double("error", 50.milliseconds))

  val callFirst = first[Int](slow, fast)
  Await.ready(callFirst, 100.milliseconds)
  println(s"first future that completed=$callFirst")


  /**
   * Exercise 4: last out of 2 futures
   */
  Thread.sleep(100)
  println("\n----- Exercise #4: get the future that completed last -----")

  // returns the last future that completes
  def last[A](futureA: Future[A], futureB: Future[A]): Future[A] = {
    val promise: Promise[A] = Promise[A]
    val lastPromise: Promise[A] = Promise[A]

    def completeLast(result: Try[A]): Unit = {
      if (!promise.tryComplete(result)) {
        lastPromise.complete(result)
      }
    }

    futureA.onComplete(completeLast)
    futureB.onComplete(completeLast)

    lastPromise.future
  }

  val callLast = last[Int](Future(double("error2", 50.milliseconds)), Future(double("4")))
  Await.ready(callLast, 100.milliseconds)
  println(s"last future that completed=$callLast")

  /**
   * Exercise 5: retry until
   */
  Thread.sleep(100)
  println("\n----- Exercise #5: -----")

  def retryUntil[A](action: () => Future[A], condition: A => Boolean): Future[A] = {
    action()
      .filter(condition)
      .recoverWith {
        case _ => retryUntil(action, condition)
      }
  }

  val random = new Random()
  val action = () => Future[Int] {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println(s"generated $nextValue")
    nextValue
  }

  val condition = (a: Int) => a < 10

  retryUntil(action, condition).foreach(result => println(s"settled at $result"))

  Thread.sleep(5000)
}
