package exercism.medium

import scala.annotation.tailrec

/**
 * https://exercism.io/my/solutions/ada9a2a325a74cd8be74431e3b8b0c02
 *
 * Given a number n, determine what the nth prime is.
 *
 * By listing the first six prime numbers: 2, 3, 5, 7, 11, and 13, we can see that the 6th prime is 13.
 *
 * If your language provides methods in the standard library to deal with prime numbers, pretend they don't
 * exist and implement them yourself.
 */
object NthPrime {
  def prime(n: Int): Option[Int] = {
    if (n <= 0) None
    else Some((Primes take n).last)
  }

  private val Primes: LazyList[Int] = 2 #:: Primes.map(p => next(p + 1))

  @tailrec
  private def next(candidate: Int): Int = {
    if (isPrime(candidate)) candidate
    else next(candidate + 1)
  }

  private def isPrime(candidate: Int): Boolean = candidate match {
    case x: Int if x <= 1 => false
    case 2 | 3 | 5 | 7 => true
    case twos: Int if twos % 2 == 0 => false
    case _ =>
      val max = candidate / 2 // changing max to sqrt(candidate) drastically speeds up isPrime for big primes
      val found = (3 to max by 2).find(candidate % _ == 0)
      found match {
        case Some(_) => false
        case _ => true
      }
  }

  // Alternative solution from the community that's much faster as it uses math.sqrt(prime) as the max in isPrime
  // https://exercism.io/tracks/scala/exercises/nth-prime/solutions/bd6b2466bc074102aeb2b35fa3e249a8
}