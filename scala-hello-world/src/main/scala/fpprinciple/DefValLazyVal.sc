/**
 * Evaluation
 * val: strict evaluation -> evaluated once during initialization and computed value cached.
 * Subsequent access will use previously computed value.
 * def: call-by-name evaluation -> evaluated (value computed) each time it is accessed.
 * lazy val: lazy evaluation -> evaluated once when it's accessed for the first time and computed value cached.
 * Subsequent access will use previously computed value.
 */
def expr: Int = {
  val valExample = {
    print("val; ")
    5
  }

  def defExample = {
    print("def; ")
    4
  }

  lazy val lazyValExample = {
    print("lazy val; ")
    3
  }

  defExample + valExample + defExample + lazyValExample + valExample + lazyValExample
}

expr // output will be: val; def; def; lazy val;

// def with or without ()
// https://blog.jessitron.com/2012/07/10/choices-with-def-and-val-in-scala/
// rule of thumb: use parentheses if the method changes state; otherwise don’t.
def spaceAge: Double = 1.0 // can only be accessed without ()
def spaceAge2(): Double = 2.0 // can be accessed with or without ()

spaceAge // ok
//spaceAge() // compilation error
spaceAge2 // ok
spaceAge2() // ok

/**
 * Always use def, not val, in a trait for abstract members.  Even if val in the trait defines a function expression,
 * can't use def to define the function - see Example below.
 *
 * https://stackoverflow.com/questions/19642053/when-to-use-val-or-def-in-scala-traits
 * A def can be implemented by a def, a val, a lazy val or an object. So it's the most abstract form of defining a member.
 * Since traits are usually abstract interfaces, saying you want a val is saying how the implementation should do.
 *
 * https://blog.jessitron.com/2012/07/10/choices-with-def-and-val-in-scala/
 * In a subclass, you can override a def with a val or a def, but you can only override a val with a val.
 * When Scala has a val, it knows the value of that expression will never change. This is not true with def;
 * therefore declaring something as val says more than declaring a def.
 */
trait MyTrait {
  def sayHello(name: String): String
  val greet: String => String
}

class MyImpl extends MyTrait {
  override def sayHello(name: String): String = s"Hello $name!"

  override val greet = sayHello
}

/**
 * Function Vs Method
 *
 * def defines a method that needs to be included within a trait, class or object.  It corresponds to a
 * Java method in a class/interface.
 * val defines a function, which gets compiled to an instance of an [anonymous] class that implements one
 * of the Function0 through Function22 traits. Scala will create 1 anonymous class and instantiate 1 object
 * from the anonymous class per val function, even if the same function expression is used for multiple vals.
 * (This behavior applies to lambda expressions as well -- see link 1 below)
 *
 * Partial functions: Scala compiler adds an additional method for PartialFunctions.  Example:
 *
 * val second: PartialFunction[List[Int],Int] = {
 *   case x :: y :: _ => y
 * }
 * The above is translated by compiler to:
 * new PartialFunction[List[Int], Int] {
 *   def apply(xs: List[Int]) = xs match {
 *     case x :: y :: _ => y
 *   }
 *   def isDefinedAt(xs: List[Int]) = xs match {
 *     case x :: y :: _ => true
 *     case _ => false
 *   }
 * }
 *
 * Java vs Scala
 * Java creates a private static method in the same class for each Java lambda expression and uses
 * "invokedynamic" to invoke the lambda expression at runtime, which is more performant, because
 * "invokedynmaic" allows the JVM to invoke lambda expressions without having to allocate a wrapper
 * object or invoke a virtual override method
 *
 * Scala functions will be invoked using "invokevirtual" by the JVM.  Scala functions are implemented using pure OO and
 * therefore incurs more performance overhead due to the additional wrapper class and object as well as invoking a
 * virtual override method (aka virtual function dispatches via vtable)
 *
 * Source Links:
 * 1. https://blog.overops.com/compiling-lambda-expressions-scala-vs-java-8/
 * 2. https://databricks.com/blog/2016/05/23/apache-spark-as-a-compiler-joining-a-billion-rows-per-second-on-a-laptop.html
 * 3. https://alvinalexander.com/scala/fp-book-diffs-val-def-scala-functions
 * 4. http://jim-mcbeath.blogspot.com/2009/05/scala-functions-vs-methods.html
 */
// Lambda expression (also called anonymous function) is a function definition that is
// not bound to an identifier. The latter is a named function
// doNothing is a function value created at runtime, whose apply method will evaluate the function literal "(_: Int) => {}"
val doNothing: Int => Unit = (x: Int) => println(s"doNothing with $x") // doNothing's type is explicitly defined
val doNothingEquivalent = new Function1[Int, Unit] { // doNothing is the syntactic sugar for doNothingEquivalent
  override def apply(x: Int) = println(s"doNothingEquivalent with $x")
}

// doNothing2's type is inferred
val doNothing2 = (x: Int) => println(s"do nothing2 with $x")
doNothing(1)
doNothing(2)
doNothingEquivalent(1)
doNothingEquivalent(2)
doNothing2(3)
doNothing2(4)
// empty-parameter
val doNothing3 = () => println("do nothing with nothing")

val lambdaExpr2: (Int, Int) => Int = (x: Int, y: Int) => {
  x + y
}
val lambdaExpr3 = (x: Int, y: Int) => {
  x + y
} // same as lambdaExpr2
val lambdaExpr4: (Int, Int) => Int = (x: Int, y: Int) => { // same as lambdaExpr3
  val z = x + y
  z
}
val lambdaExpr5 = (x: Int, y: Int) => x + y // same as lambdaExpr4
val lambdaExpr6 = (_: Int) + (_: Int) // same as lambdaExpr5, _ must appear only once for each parameter

lambdaExpr4(5, 6)

def doNothingDef(): Unit = {
  println("doNothingDef: I actually do something")
}
def doNothingDef2: Unit = {
  println("doNothingDef2: I actually do something")
}
doNothingDef
doNothingDef2

// Example 1: use val to define a function --- add is an instance of Function2 ---
// "add" name assigned to the anonymous function
// "(x: Int, y: Int) => x + y"  the anonymous function
// val functions are concrete instances of Function0 through Function22, e.g. "add"" is an instance of Function2.
// Each time a val function is called, it translates to the "apply" method inherited from the FunctionX trait.
// Because val functions are instances of Function0 through Function22, there are several methods available on
// these instances, including andThen, compose, and toString.
val add = (x: Int, y: Int) => x + y // same as "val add: (Int, Int) => Int = (x: Int, y: Int) => x + y"
add(3, 4) // Scala translates this syntactic sugar to "add.apply(3, 4)"
add.apply(3, 4) // add is an instance of an anonymous class that implements the trait Function2
add.toString // toString is another method implemented on Function2
add.isInstanceOf[Function2[_, _, _]] // true
add.isInstanceOf[Function1[_, _]] // false

class Add2 extends Function2[Int, Int, Int] {
  def apply(a: Int, b: Int) = a + b
}

val add2 = new Add2
add2.toString()
add2.isInstanceOf[Function2[Int, Int, Int]]

// using anonymous class
val add3 = new Function2[Int, Int, Int] {
  def apply(a: Int, b: Int) = a + b
}
add3.toString()
add3.isInstanceOf[Function2[Int, Int, Int]]

// Technically, "sum" is not a function.  It is a method that needs to be defined within a class or object.
// By all accounts, creating a Scala def method creates a standard method in a Java class.
def sumMethod(x: Int, y: Int, z: Int): Int = x + y + z
sumMethod(3, 4, 5)
val sumFunction = sumMethod _ // The _ turns a method into a function object.  "sum _" is a partially applied function
sumFunction(0, 2, 8)

// Since only one argument is missing, the Scala compiler generates a new function class whose apply
// method takes one argument. When invoked with that one argument, this generated function's apply
// method invokes sum, passing in 1, the argument passed to the function, and 3
val sumFunction1 = sumMethod(1, _, 3) // Int => Int
sumFunction1(2) // 1 + 2 + 3
val sumFunction2 = sumMethod(1, _, _) // (Int, Int) => Int
sumFunction2(10, 11) // 1 + 10 + 11
(sumMethod _).toString // we can now call all methods available on the function object
(sumMethod _).apply(4, 5, 6) // Can also use syntactic sugar sum(4, 5)

// add4 is a function object, a different instance from "sumMethod _"
val add4 = sumMethod _ // add4: (Int, Int, Int) => Int = <function>
//-----------------------------------------------------------------
// eta-expansion:
// In general, if Scala expects a function type, you can pass it a method name and have it automatically converted
// to a function. For example, if you are calling a method that accepts a function as one of its parameters,
// you can supply as that argument a method of the appropriate signature without having to include the trailing
// underscore. This is called eta-expansion.
//-----------------------------------------------------------------
val add5: (Int, Int, Int) => Int = sumMethod // add5: (Int, Int, Int) => Int = <function>
println("sumMethod _")
(sumMethod _).apply(3, 4, 8) // (sumMethod _) is a function
(sumMethod _).toString // res30: String = <function>
(sumMethod _).isInstanceOf[(Int, Int, Int) => Int] // true
(sumMethod _).isInstanceOf[Function3[Int, Int, Int, Int]] // true

//-----------------------------------------------------------------
// Closure:
// It makes no difference that the y in this case is a parameter to a method call that has already returned.
// The Scala compiler rearranges things in cases like these so that the captured parameter lives out on the heap,
// instead of the stack, and thus can outlive the method call that created it.
//-----------------------------------------------------------------
def methodThatReturnsFunction(y: Int): Int => Int = (x: Int) => x + y // a method can also return a function
val plus5 = methodThatReturnsFunction(5) // Int => Int
plus5(3) // 5 + 3
methodThatReturnsFunction(5)(3) // same as plus5(3)
val plus10 = methodThatReturnsFunction(10)
plus10(3) // 10 + 3

// with def you can write a function that takes a generic type, but not with val
def m1[A]: A => Int = (a: A) => a.toString.length
val f = m1[Int] // coerce a parameterized method into a function
m1[Int](3456)
f(345)
f.toString()
f.isInstanceOf[Function1[Int, Int]]

def m2[A](a: A): Int = a.toString.length
val f2 = m2[Int] _ // TODO notice the _, which tells Scala to treat m2[Int] as a function rather than taking the value generated by a call to that method
m2[Int](3456)
f2(345)
f2.toString
f2.isInstanceOf[Function1[Int, Int]]

// explicitly define the type for f3, which is "(Int) => Int"
// since the type for f3 is explicitly defined, Scala will automatically convert the method "m2[Int]" to the expected function
val f3: Int => Int = m2[Int]

// function values can be defined as vars as well.
var increase = (x: Int) => x + 1
increase(21)
increase = (x: Int) => x * 2
increase(21)
// error: type mismatch;
// found   : (Int, Int) => Int
// required: Int => Int
// increase = (x: Int, y: Int) => x + y