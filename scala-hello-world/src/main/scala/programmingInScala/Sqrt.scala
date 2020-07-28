package programmingInScala

object Sqrt extends App {

  println(s"sqrt(4)=${sqrt(4)}")
  println(s"sqrt(0.25)=${sqrt(0.25)}")

  // write: double sqrt(double x) using the strategy of binary search, x is guaranteed to be non-negative
  // MAX_DOUBLE is approximately 1.7 x 10^308
  // test values for x: 0.25, 1e300

  def sqrt(x: Double): Option[Double] = {
    x match {
      case d if d < 0.0 => None
      case 0 | 1 => Some(x)
      case d if d >= 1.0 => Some(findSqrt(x, 1.0, x))
      case d if d < 1.0 => Some(findSqrt(x, x, 1.0))
    }
  }

  private def findSqrt(x: Double, left: Double, right: Double): Double = { // x > = 1.0
    val guess = (right + left)/2 // 2.5
    val guessTestResult = testGuess(guess, x)

    if (guessTestResult == 0) {
      guess
    } else if (guessTestResult < 0) {
      findSqrt(x, guess, right)
    } else { // too big
      findSqrt(x, left, guess)
    }
  }

  private def testGuess(guess: Double, x: Double): Int = { // -1, 0, 1
    // val result = (x / guess) - guess // TODO bug here
    val result = guess - (x / guess)

    if (Math.abs(result) <= 0.0001) {
      0
    } else if (result < 0.0) {
      -1
    } else {
      1
    }
  }
}
