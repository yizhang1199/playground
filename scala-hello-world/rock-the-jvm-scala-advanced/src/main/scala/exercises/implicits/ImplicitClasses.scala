package exercises.implicits

import scala.util.Try

/**
 * Purpose:
 * Provides extension methods to existing types (classes or interfaces), one at a time, without modifying existing
 * code, aka Decorator pattern.
 *
 * An implicit class must be defined in a scope where method definitions are allowed (not at the top level).
 * Type erasure: compiler looks for implicit conversion at compile time, e.g. before types are erased.
 *
 * Best Practices:
 * 1. Keep type enrichment to implicit classes and type classes.
 * 2. Avoid implicit def methods as much as possible because it is very difficult to trace and debug.
 *    If must, use specific/custom types, never generic types such as String.
 * TODO how is debugging easier for regular implicit classes?
 * 3. Always make an implicit class a value class if possible.
 * 4. Package implicits clearly, bring into scope what you need and when you need it.
 *
 * Comparisons:
 * 1. Type classes together with context bounds, are used to provide extensions to many existing classes.
 * 2. Value classes are used to provide better runtime performance by avoiding allocation of the wrapper class.
 *    When possible, always make an implicit class a value class.
 *
 * Resources:
 * https://docs.scala-lang.org/overviews/core/implicit-classes.html
 * https://docs.scala-lang.org/overviews/core/value-classes.html
 *
 */
object ImplicitClasses extends App {

  /**
   * An implicit class is desugared into a class and implicit method pairing, where the implicit method mimics
   * the constructor of the class, e.g. "implicit final def RichInt(n: Int): RichInt = new RichInt(n)"
   *
   * Without extending from AnyVal, an instance of the implicit class will always be created every time the
   * implicit conversion happens.  See examples below.
   */
  // Example 1
  implicit class MyEnrichedString(strVal: String) {
    def asInt: Option[Int] = {
      Try(strVal.toInt).toOption
    }
  }
  // Example 2 (equivalent to Example 1 but intentionally using different name to avoid compilation errors).
  class MyEnrichedString2(strVal: String) {
    def asInt2: Option[Int] = {
      Try(strVal.toInt).toOption
    }
  }
  // implicit def methods should be avoided as much as possible as it is very difficult to trace and
  // debug if there is a bug in the implicit def method.  It is also an advanced language feature that has
  // to be explicitly enabled, by either: adding "import scala.language.implicitConversions" or
  // by setting the compiler option -language:implicitConversions
  import scala.language.implicitConversions
  implicit def toMyEnrichedString(strVal: String): MyEnrichedString2 = new MyEnrichedString2(strVal)

  /**
   * When possible, always make the implicit class a value class, by extending from AnyVal, for allocation-free
   * extension methods; however, due to limitations for value classes, it may not always be possible.
   * The compiler will create a companion object for MyEnrichedInt, e.g. MyEnrichedInt$.  At runtime,
   * an expression "3 times 5" will be optimised to the equivalent of a method call on a static object, e.g.
   * MyEnrichedInt$.MODULE$.extension$times(5), rather than a method call on a newly instantiated object.
   */
  implicit class MyEnrichedInt(val intVal: Int) extends AnyVal {
    def repeats(op: => Unit): Unit = {
      (1 to intVal).foreach(_ => op)
    }
  }

  val AsIntTestSome = "1234".asInt // Some(1234)
  val AsIntTestNone = "blah".asInt // None
  MyEnrichedString("1234").asInt // TODO There is no ImplicitClasses.MyEnrichString$.class, why does this work?
  println(s"AsIntTestSome=$AsIntTestSome AsIntTestNone=$AsIntTestNone")

  2 repeats println("Hello World!")
  // Compiler auto-generated companion object for MyEnrichedInt, e.g. ImplicitClasses.MyEnrichedInt$.class
  MyEnrichedInt(3) repeats println("Hello Kitty!")

  /**
   * Context bounds and implicitly
   */
  // implicitly
  implicit val MyString: String = "Hello World!"
  val TestImplicitly = implicitly[String]
  println(s"TestImplicitly=$TestImplicitly")

  // Context Bound.  "[A : B]" is syntactic sugar for "implicit myVar: B[A]"
  def g[A](a: A)(implicit numeric: Numeric[A]): String = s"Hello implicit world $a times!"
  def f[A : Numeric](a: A): Unit = {
    val myImplicitParameter = implicitly[Numeric[A]] // Not needed if f simply passes it on to g
    println(s"f: myImplicitParameter=$myImplicitParameter; g($a)=${g(a)}")
  }
  f[Int](10)
}