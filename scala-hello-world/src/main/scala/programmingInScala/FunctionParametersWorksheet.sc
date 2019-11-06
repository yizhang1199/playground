// Repeated parameters: variable length argument lists
def echo(args: String*) = {
  println("args=" + args)
  for (arg <- args) println(arg)
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
speed(time=10, distance=100)

// Default arguements
def greet(out: java.io.PrintStream = Console.out,
          name: String = "World") : Unit = {
  out.println(s"Hello $name")
}
greet()
greet(name="Kitty")
greet(out=Console.err)

// By-name parameters (mostly for functions that take no arguements?)
// https://learning.oreilly.com/library/view/programming-in-scala/9780981531687/control-abstraction.html
def expensiveTrue() : Boolean = {
  println("expensiveTrue: taking lots of time")
  true
}
// p is a "normal" parameter that represents a function that takes no arguement and returns Boolean
def myAssertWithFunction(assertionEnabled: Boolean, p: () => Boolean): Unit = {
  if (assertionEnabled && p()) println("myAssertWithFunction: assertError!")
}
myAssertWithFunction(true, expensiveTrue) // expensiveTrue will be called
myAssertWithFunction(false, expensiveTrue) // expensiveTrue will not be called

// p is a by-name parameter that represents a function that takes no arguement and returns Boolean
def myAssertWithByNameParameter(assertionEnabled: Boolean, p: => Boolean): Unit = {
  if (assertionEnabled && p) println("myAssertWithByNameParameter: assertError!")
}
myAssertWithByNameParameter(true, expensiveTrue) // expensiveTrue will be called
myAssertWithByNameParameter(false, expensiveTrue) // expensiveTrue will not be called

def myAssertWithBolleanParameter(assertionEnabled: Boolean, p: Boolean): Unit = {
  if (assertionEnabled && p) println("myAssertWithBolleanParameter: assertError!")
}
myAssertWithBolleanParameter(true, expensiveTrue) // expensiveTrue will be called
myAssertWithBolleanParameter(false, expensiveTrue) // expensiveTrue will be called because it must be evaluated before passing p to myAssertWithBolleanParameter

/**
 * myAssertWithByNameParameter: a function value will be created whose apply method will evaluate 5 > 3
 * before myAssertWithByNameParameter is called. If assertionEnabled is false, "5>3" will NOT
 * be evaluated.
 *
 * myAssertWithBolleanParameter: "5>3" will always be evaluated before myAssertWithBolleanParameter is called.
 */

// A parameter-less method:
// (Convention) Has NO side effects and accesses mutable state only by reading fields of the containing object
// Called using field selection syntax.
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
value2
value2()

val x2: AnyRef = List()