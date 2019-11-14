// Repeated parameters: variable length argument lists
def echo(args: String*) = {
  println("args=" + args)
  for ( arg <- args ) println(arg)
}
echo()
echo("Hello", "Kitty")

val names = Array("Hello", "Kitty", "!")
//echo(names) // does not compile
// This notation tells the compiler to pass each element of arr as its own argument to echo,
// rather than all of it as a single argument.
echo(names: _*)
echo(List("Hello", "too"): _*)

// Named arguements, frequently used with default arguements
// TODO tight coupling?  What if parameter names change?
def speed(distance: Float, time: Float): Float = distance / time
speed(100, 10)
speed(time = 10, distance = 100)

// Default arguements
def greet(out: java.io.PrintStream = Console.out,
          name: String = "World"): Unit = {
  out.println(s"Hello $name")
}
greet()
greet(name = "Kitty")
greet(out = Console.err)

/**
 * By-name parameters are only evaluated when used. They are in contrast to by-value parameters.
 * To make a parameter called by-name, simply prepend => to its type.
 *
 * By-name parameters: only evaluated when used in the function body
 * By-value parameters: always evaluated, but only evaluated once
 *
 * https://docs.scala-lang.org/tour/by-name-parameters.html
 **/
// both condition and body are by-name parameters
def whileLoop(condition: => Boolean)(body: => Unit): Unit = {
  if (condition) {
    body
    whileLoop(condition)(body)
  }
}
var i = 2
whileLoop(i > 0) {
  println(s"in whileloop: i=$i")
  i -= 1
}
// whileLoop2 has the same effect as whileLoop but uses empty-param functions,
// e.g. condition is a function that take no parameters and returns a Boolean.
def whileLoop2(condition: () => Boolean)(body: () => Unit): Unit = {
  if (condition()) {
    body()
    whileLoop(condition())(body())
  }
}
i = 2
whileLoop2(() => i > 0) { // notice the cumbersome syntax required
  () => {
    println(s"in whileloop2: i=$i")
    i -= 1
  }
}
// since condition is a by-value parameter, it's only evaluated once.  We have
// an infinite loop if condition evaluates to true when infiniteloop is called
def infiniteloop(condition: Boolean)(body: => Unit): Unit = {
  if (condition) {
    body
    infiniteloop(condition)(body)
  }
}

/**
 * Parameter-less method vs empty-param method
 */
// A parameter-less method:
// (Convention) Has NO side effects and accesses mutable state only by reading fields of the containing object
// Called using field selection syntax, e.g. without ()
// Can be changed (in the same class) to a field without impacting client code, e.g. val value = seed + 2
// Can be overridden (in sub classes) with a val
var seed = 3
def value: Int = seed + 2

// An empty-param method:
// (Convention) Has side effects or depends on mutable state outside the containing object
// (Convention) Should be called with (), e.g. value2()
// Can be called without () but shouldn't as the field selection syntax can be misleading.
// Will also generate a compilation warning.
def value2(): Int = {
  println("I have some side effects")
  seed + 2
}

value
//value() // won't compile
value2 // legal but trigger compiler warning
value2()