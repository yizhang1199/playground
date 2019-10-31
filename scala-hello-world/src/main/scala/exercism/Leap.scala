package exercism

/**
 * Leap year definition:
 *
 * 1. on every year that is evenly divisible by 4
 * 2. except every year that is evenly divisible by 100
 * 3. unless the year is also evenly divisible by 400
 */
object Leap {
  def leapYear(year: Int): Boolean = {
    def divisibleBy(div: Int) = year % div == 0
    divisibleBy(4) && (!divisibleBy(100) || divisibleBy(400))
  }
}
