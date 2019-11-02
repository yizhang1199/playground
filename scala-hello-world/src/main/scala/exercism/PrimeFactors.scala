package exercism

import scala.annotation.tailrec

/**
 * https://exercism.io/my/solutions/db0f77ce1025493881e6c9b72e18897c
 *
 * Compute the prime factors of a given natural number.
 *
 * A prime number is only evenly divisible by itself and 1.
 *
 * Note that 1 is not a prime number.
 * Example
 *
 * What are the prime factors of 60?
 *
 * Our first divisor is 2. 2 goes into 60, leaving 30.
 * 2 goes into 30, leaving 15.
 * 2 doesn't go cleanly into 15. So let's move on to our next divisor, 3.
 * 3 goes cleanly into 15, leaving 5.
 * 3 does not go cleanly into 5. The next possible factor is 4.
 * 4 does not go cleanly into 5. The next possible factor is 5.
 * 5 does go cleanly into 5.
 * We're left only with 1, so now, we're done.
 *
 * Our successful divisors in that computation represent the list of prime factors of 60: 2, 2, 3, and 5.
 *
 * You can check this yourself:
 *
 * 2 * 2 * 3 * 5
 * = 4 * 15
 * = 60
 * Success!
 */
object PrimeFactors {
  def factors(number: Long): List[Long] = {
    require(number > 0)

    lazy val lazyRange: LazyList[Long] = 2 #:: lazyRange.map(_ + 1)

    @tailrec
    def factor(factors: List[Long], number: Long): List[Long] = number match {
      case 1 => factors
      case _ =>
        val firstFactor = lazyRange.find(number % _ == 0).get
        val remainder = number / firstFactor
        factor(firstFactor :: factors, remainder)
    }

    factor(List(), number).reverse
  }

  /**
   * A more efficient implementation from the community.  It's more efficient because it will skip div already tried, e.g.
   * if number is not divisible by div-1, then (number/div) won't be divisible by div-1 either.  Also, it does not require
   * an additional sequence for div.
   *
   * @param number
   * @return
   */
  def factorsFaster(number: Long): List[Long] = {
    @tailrec
    def findFactors(num: Long,
                    div: Long = 2,
                    acc: List[Long] = List()): List[Long] =
      div match {
        case d if num % d == 0L =>
          findFactors(num / div, div, div :: acc)
        case d if d >= num => acc.reverse
        case _ => findFactors(num, div + 1, acc)
      }

    findFactors(number)
  }
}
