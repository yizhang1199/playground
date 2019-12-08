/**
 * Case classes are: <br>
 * 1. Immutable
 *
 * 2. Decomposable through pattern matching
 * (Only the Primary constructor.  Auxiliary constructors cannot be used in pattern matching)
 *
 * 3. Allows for comparison based on structure instead of reference
 *
 * 4. Easy to use and manipulate:
 * # case class is actually a shorthand which, in addition to defining a class, also
 * creates a companion object with a few methods: apply, unapply, copy, etc.
 * We can still define companion object for a case class (in order to define a companion object for a
 * class you have to set the same names for them and declare them in the same file.) and define other methods.
 * # Each argument in the primary constructor has a corresponding getter generated.
 */
case class Rectangle(length: Double, width: Double) { // Primary constructor

  def this(height: Double) { // Auxiliary constructors cannot be used in pattern matching
    this(height, 50.0)
  }

  def area(): Double = length * width
  override def toString: String = s"length=$length, width=$width"
}

object Rectangle {
  def area(rectangle: Rectangle) = rectangle.area()

  // If apply is explicitly defined, it will be used instead of the default.
  //def apply(length: Double, width: Double): Rectangle = new Rectangle(0, 0)
}

// Extract values using auto-generated "unapply"
val r1 = Rectangle(5, 4)
r1.area()
Rectangle.area(r1)

val Rectangle(length, width) = r1
println(s"Values extracted from Rectangle.unapply: length=$length, width=$width")

class NotCase(x: Int) { // a regular class cannot be used in pattern matching
}

val x = new Rectangle(160)
val y = Rectangle(170, 60) // same as BmilCalc.apply
val z = Rectangle.apply(180, 100) // apply is auto-generated for case classes

y == Rectangle(170, 60) // true: case classes compare by value not reference

val a = x.copy() // shallow copy

a == x // true
x == y // false

x.length + x.width // auto-generated getters

def testPatternMatching(x: Any): Unit = {
  x match {
    case Rectangle(height, weight) => println(s"Rectangle($height, $weight)")
    //case NotCase(x) =>
    case _ => println("Not a stat: " + x)
  }
}

testPatternMatching(x)
testPatternMatching(y)
testPatternMatching(123)