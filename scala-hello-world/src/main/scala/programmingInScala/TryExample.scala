package programmingInScala

import scala.annotation.tailrec
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object TryExample {

  def main(args: Array[String]): Unit = {
    TryExample.divide
  }

  @tailrec
  def divide: Try[Int] = {
    val dividend = Try[Int](StdIn.readLine("Enter an Int that you'd like to divide: ").toInt)
    val divisor = Try(StdIn.readLine("Enter an Int that you'd like to divide by: ").toInt)
    val problem: Try[Int] = dividend.flatMap(x => divisor.map(y => x / y))
    problem match {
      case Success(v) =>
        println("Result of " + dividend.get + "/" + divisor.get + " is: " + v)
        Success(v)
      case Failure(e) =>
        println("You must've divided by zero or entered something that's not an Int. Try again!")
        println("Info from the exception: " + e.getMessage)
        divide
    }
  }
}
