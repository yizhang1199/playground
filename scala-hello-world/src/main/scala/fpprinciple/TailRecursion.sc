import scala.annotation.tailrec

// https://www.geeksforgeeks.org/tail-call-elimination/
// https://www.scala-exercises.org/scala_tutorial/tail_recursion

def x: Int = 2; val y = 5;
def add(x: Int, y: Int): Int = x + y
add(x, 2)

x.+(34)
y.+(x)

@tailrec
def gcd( a: Int, b: Int): Int = {
  if (b==0) a
  else gcd (b, a % b)
}

def factorial( x: BigInt ): BigInt = {
  @tailrec
  def tailRecursiveCalculation(n: BigInt, total: BigInt): BigInt = {
    if (n <= 1) total
    else tailRecursiveCalculation(n-1, n * total)
  }
  tailRecursiveCalculation(x, 1)
}

def factorial2( x: BigInt ): BigInt = {
  var total: BigInt = 1;

  @tailrec
  def tailRecursiveCalculation(n: BigInt): BigInt = {
    if (n <= 1) total
    else {
      total *= n
      tailRecursiveCalculation(n - 1)
    }
  }
  tailRecursiveCalculation(x)
}

def factorial3( x: Int ): BigInt = {
  if (x <= 2) x
  else {
    val initVal: BigInt = 1
    (2 to x).foldLeft(initVal)((initVal, num) => initVal * num)
  }
}

factorial(55)
factorial2(55)
factorial3(55)

val list = List(1, 2, 4)
list.map(element => element * 2)
list.map{element => element * 2}

def boom(x: Int): Int = {
  if (x == 0) throw new Exception("boom!")
  else boom(x - 1) + 1
}

@tailrec
def boom2(x: Int): Int = {
  if (x == 0) throw new Exception("boom!")
  else boom2(x - 1)
}

/**
 * boom(3) results in 4 stack frames, as shown by the stace trace below
 * java.lang.Exception: boom!
 * at .boom(<console>:2)
 * at .boom(<console>:3)
 * at .boom(<console>:3)
 * at .boom(<console>:3)
 * ... 30 elided
 */
//boom(3)
/**
 * boom2(3) will only result in a single stack frame, due to tail recursion optimization
java.lang.Exception: boom!
at .boom2(<console>:3)
... 30 elided
 */
//boom2(3)

// Scala only optimizes directly recursive calls back to the same function making the call.
// nestedFun cannot be marked @tailrec as it does not call itself
// funValue refers to a function value that essentially wraps a call to nestedFun
val funValue = nestedFun _
//@tailrec  -- Scala compiler will mark this as an error
def nestedFun(x: Int) : Unit = {
  if (x != 0) { println(x); funValue(x - 1) }
}