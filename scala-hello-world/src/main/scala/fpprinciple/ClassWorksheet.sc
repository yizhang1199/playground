import scala.annotation.tailrec
import math.abs

class Rational(n: Int, d: Int) { // Primary constructor
  /**
   * The Scala compiler will compile any code you place in the class body,
   * which isn't part of a field or a method definition, into the primary constructor.
   */
  require(d != 0, "denominator must not be 0")
  def this(n: Int) = { // Auxiliary constructor
    this(n, 1)
  }

  @tailrec
  private def gcd(a: Int, b: Int): Int = { // greatest common denominator
    if (b == 0) a else gcd(b, a % b)
  }

  private val g: Int = abs(gcd(n, d))
  println("n=" + n + ", d=" + d + ", gcd=" + g)

  val numerator = n / g
  val denominator = d / g

  def add(that: Rational): Rational = {
    new Rational(this.numerator * that.denominator + this.denominator * that.numerator,
      this.denominator * that.denominator)
  }

  def sub(that: Rational): Rational = {
    //    new Rational(this.numberator * that.denominator - this.denominator * that.numberator,
    //      this.denominator * that.denominator)
    add(that.neg)
  }

  def neg: Rational = {
    new Rational(-this.numerator, this.denominator)
  }

  def less(that: Rational): Boolean =
    this.numerator * that.denominator < this.denominator * that.numerator

  override def toString: String = {
    numerator + "/" + denominator
  }
}

val x = new Rational(1, 3)
val y = new Rational(5, 7)
new Rational(5)
//val z = new Rational(9, 0)

// infix notation can be used for any method with a single param
x add y  // same as x.add(y)
x sub y
x.neg

x.add(y) less x
x add y less x