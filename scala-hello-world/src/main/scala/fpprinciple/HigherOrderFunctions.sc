import scala.annotation.tailrec

/**
 * A higher order function is a function that takes a function as an argument, or returns a function.
 */

def cube(a: BigInt) : BigInt = {
  a * a * a
}

def sum(f: BigInt => BigInt,
        a: BigInt,
        b: BigInt): BigInt = {
  @tailrec
  def helper(a: BigInt, total: BigInt) : BigInt = {
    if (a > b) total
    else helper(a + 1, f(a) + total)
  }
  helper(a, 0)
}

// return a function: (BigInt, BigInt) => BigInt
def sum2(f: BigInt => BigInt): (BigInt, BigInt) => BigInt = {
  def helper(a: BigInt, b: BigInt) : BigInt = {
    if (a > b) 0
    else f(a) + helper(a + 1, b)
  }
  helper
}

// Syntactic sugar for sum2
def sum3(f: BigInt => BigInt)(a: BigInt, b: BigInt): BigInt = {
  @tailrec
  def helper(a: BigInt, total: BigInt) : BigInt = {
    if (a > b) total
    else helper(a + 1, f(a) + total)
  }
  helper(a, 0)
}

def sum4(f: BigInt => BigInt)(a: BigInt)(b: BigInt) : BigInt = {
  @tailrec
  def helper(a: BigInt, total: BigInt) : BigInt = {
    if (a > b) total
    else helper(a + 1, f(a) + total)
  }
  helper(a, 0)
}
def sumCubes2 = sum2(cube)
def sumCubes3 = sum3(cube) _

// generally, function application associates to the left, e.g. sum2(cube)(2,4) is the same as (sum2(cube))(2,4)
sum(cube, 2, 4) == sum(x => x * x * x, 2, 4)
sum(cube, 2, 4) == sum2(cube)(2, 4)
sum(cube, 2, 4) == sum3(cube)(2, 4)
sum(cube, 2, 4) == sum4(cube)(2)(4)
sum(cube, 2, 4) == ((sum4(cube)) (2)) (4)
sum(cube, 2, 4) == sumCubes2(2, 4)
sum(cube, 2, 4) == sumCubes3(2, 4)

// function type associates to the right
sum2(cube)  // (BigInt, BigInt) => BigInt = <function>
sumCubes2   // (BigInt, BigInt) => BigInt = <function>
sum3(cube) _  // (BigInt, BigInt) => BigInt = <function>
sum4(cube) _  // BigInt => (BigInt => BigInt) = <function>

sum _   // (BigInt => BigInt, BigInt, BigInt) => BigInt = <function>
sum2 _  // (BigInt => BigInt) => ((BigInt, BigInt) => BigInt) = <function>
sum3 _  // (BigInt => BigInt) => ((BigInt, BigInt) => BigInt) = <function>
sum4 _  // (BigInt => BigInt) => (BigInt => (BigInt => BigInt)) = <function>

// from a to b, mapper(a) combiner mapper(a+1) combiner ... mapper(b)
// if a > b, the value is combiner specific
def mapReduce(mapper: BigInt => BigInt, combiner: (BigInt, BigInt) => BigInt, combinerDefault: BigInt)
             (a: BigInt, b: BigInt) : BigInt = {
  if (a > b) combinerDefault
  else combiner(mapper(a), mapReduce(mapper, combiner, combinerDefault)(a + 1, b))
}

mapReduce(m => m, (x, y) => x * y, 1) (2, 4)
mapReduce(m => m * m, (x, y) => x + y, 0) (2, 4)

def callTwice(value: Int)(f: Int => Int) = {
  f(f(value))
}

// When a single argument is passed in, {} can be used instead of ():
// The {} makes callTwice(3) feel more like a built-in control structure since function literals are written between {}
// Additional statements can be added in {}; whereas, the println will result in compilation error if placed in ()
callTwice(3)(_ * 2)
callTwice(3) { // {} can be used only if a single argument is passed in
  println ("callTwice with {} for the lambda function")
  x => x * 2
}