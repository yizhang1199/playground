package programmingInScala.leetCode

import scala.annotation.tailrec

object PowerOfSolution extends App {

  assert(Math.pow(0.00001, Integer.MAX_VALUE) == powerOf(0.00001, Integer.MAX_VALUE))
  assert(Math.pow(3.23456, Integer.MIN_VALUE) == powerOf(3.23456, Integer.MIN_VALUE))
  assert(Math.pow(2.0, 10) == powerOf(2.0, 10))
  assert(Math.pow(2.0, 15) == powerOf(2.0, 15))
  assert(Math.pow(-2.0, 10) == powerOf(-2.0, 10))
  assert(Math.pow(-2.0, 15) == powerOf(-2.0, 15))

  /**
   * @param x between (-100.0, 100.0)
   * @param n Any Integer: [-2147483648, 2147483647]
   * @return
   */
  def powerOf(x: Double, n: Int): Double = {
    require(x > - 100.0 && x < 100.0)
    powerOfTR(x, n)
  }

  @tailrec
  private def powerOfTR(x: Double, n: Int, accumulator: Double = 1.0): Double = n match {
    case 0 => 1.0
    case 1 => x * accumulator
    case Even() => powerOfTR(x * x, n/2, accumulator)
    case Positive() => powerOfTR(x * x, n/2, x * accumulator)
    case Integer.MIN_VALUE =>
      val d = 1.0 / x
      powerOfTR(d, Integer.MAX_VALUE, d)
    case _ => powerOfTR(1.0 / x, Math.abs(n))
  }
}

object Positive {
  def unapply(value: Int): Boolean = value > 0
}

object Even {
  def unapply(value: Int): Boolean = value > 0 && value % 2 == 0
}

