// Stream has been deprecated and replaced by LazyList starting from 2.13.0
def from(n: Int): LazyList[Int] = {
  n #:: from(n + 1)
}

from(1).take(19).toList

// The list of numbers must start at 2 for the algorithm to be correct (Sieve of Eratosthenes)
def sieve(list: LazyList[Int]): LazyList[Int] = {
  list.head #:: list.tail.filter(p => p % list.head != 0)
}

def primes: LazyList[Int] = sieve(from(2))
primes.take(100).toList

def squareRoot(n: Double): Seq[Double] = {

  def isGoodEnough(guess: Double): Boolean = {
    Math.abs(n - guess * guess) <= 0.00001
  }

  def nextGuess(currentGuess: Double): Double = {
    (currentGuess + n / currentGuess) / 2
  }

  lazy val guesses: LazyList[Double] = 1.0 #:: (guesses map nextGuess)

  guesses.filter(p => isGoodEnough(p)).take(5).toList
}

squareRoot(5.0)