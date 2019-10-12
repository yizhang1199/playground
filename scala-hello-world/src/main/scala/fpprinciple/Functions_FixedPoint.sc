import scala.math.abs

val tolerance = 0.0001

// a number x is called a fixed point of a function such as f(x) = x, e.g. given f = 1 + x/2, the fixed point of f is 2
def isCloseEnough(guess: Double, x: Double): Boolean = {
  abs(((x - guess) / x) / x) <= tolerance
}

// for some functions, the fixed point can be approximated by applying f to an initial estimate repeatedly: x, f(x), f(f(x)), f(f(f(x))), ...
def fixedPoint(f: Double => Double)(initialEstimate: Double): Double = {
  def loop(x: Double): Double = {
    val fx = f(x)
    if (isCloseEnough(x, fx)) x
    else loop(fx)
  }

  loop(initialEstimate)
}

fixedPoint(x => (1 + x / 2))(1)
fixedPoint(x => (1 + x / 2))(-10)
fixedPoint(x => (1 + x / 2))(10)

// both averageDamp(f) and averageDamp2(f) return a function but averageDamp is much simpler
def averageDamp(f: Double => Double)(x: Double): Double = (x + f(x)) / 2
def averageDamp2(f: Double => Double): Double => Double = {
  def temp(x: Double): Double = (x + f(x)) / 2

  temp
}

def sqrt(x: Double): Double = {
  fixedPoint(averageDamp(y => x / y))(x / 2)
}

sqrt(2)
sqrt(.2)
