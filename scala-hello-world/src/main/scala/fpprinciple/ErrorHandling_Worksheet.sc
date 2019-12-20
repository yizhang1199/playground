import scala.util.control.Exception._

def toErrorString(t: Throwable): String = s"Error: ${t.getMessage}"

val withThrowableToString: Catch[String] = handling(classOf[RuntimeException]) by toErrorString

def doubleEven(x: Int): Int = {
  require(x % 2 == 0, "input must be an even number")
  2 * x
}

// errorResult: Any = Error: requirement failed: input must be an even number
val errorResult: Any = withThrowableToString { doubleEven(3) }
errorResult match {
  case s: String => println(s"got error string $s")
  case num: Int => println(s"got Int $num")
}

// successResult: Any = 8
val successResult: Any = withThrowableToString { doubleEven(4) }
println(s"successResult=$successResult")