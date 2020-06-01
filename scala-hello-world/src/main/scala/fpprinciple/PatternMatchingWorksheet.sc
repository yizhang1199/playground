

class Person(val name: String, val age: Int)

/**
 * For pattern matching, the object does NOT need to have the same name as the class
 *
 * Extractor objects: https://docs.scala-lang.org/tour/extractor-objects.html
 */
object Person {
  def apply(name: String, age: Int): Person = new Person(name, age)

  def unapply(person: Person): Option[(String, Int)] =
    if (person.age >= 21) Some((person.name, person.age))
    else None

  def unapply(age: Int): Option[String] =
    if (age < 0) None
    else if (age >= 21) Some("major")
    else Some("minor")
}

val bob = Person("Bob", 20)
val greetings = bob match {
  case Person(name, age) => s"Hi $name, you are $age yo."
  case _ => "Hi nobody"
}

val legalStatus = 21 match {
  case Person(status) => s"You are a $status"
  case _ => s"unknown legal status"
}

object even {
  def unapply(n: Int): Option[Boolean] = n match {
    case x if x % 2 == 0 => Some(true)
    case _ => None // caller must know None is used for no match
  }
}

object singleDigit {
  def unapply(n: Int): Option[Boolean] =
    if (n > -10 && n < 10) Some(true)
    else Some(false) // caller must know false is used for no match
}

// Preferred for readability: syntactical sugar for unapply that returns Option[Boolean]
// Also see exercism.Bob for a simper example
object negative {
  def unapply(n: Int): Boolean = n < 0
}

for ( i <- List(2, 3, 11, -9) ) {
  println(i match {
    case negative() => println(s"$i is negative")
    case even(_) => println(s"$i is even")
    case singleDigit(true) => println(s"$i is a single digit")
    case _ => println(s"I don't know what $i is")
  })
}

// infix patterns
case class Or(number: Int, name: String) // our own Either
val two = Or(2, "two")
val orTest = two match {
  case Or(number, name) => println(s"$number is written as $name")
}
val orTest2 = two match {
  case number Or name => println(s"$number is written as $name")
}
val infixPatternTest = List(1, 2, 3) match {
  // here :: is a final case class!!!
  case ::(1, tail) => println(s"A list that starts with 1 and ends with $tail")
  // using infix
  case head :: tail => println(s"A list that starts with $head and ends with $tail")
  case x => println(s"Did not match any pattern: $x")
}

// Either: Convention dictates that Left is used for failure and Right is used for success.
val either1: Either[Int, String] = Left(2)
val either2 = Right("two") // Right[Nothing, String]
val either3 = Left("blah") // Left(String, Nothing

// decomposing sequences
val numbers = List(1)
val vararg = numbers match {
  case List(1, _*) => println("starting with 1")
}

abstract class MyList[+A] {
  def head: A = ???

  def tail: MyList[A] = ???
}

case object Empty extends MyList[Nothing]

case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

object MyList {
  def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
    if (list == Empty) Some(Seq.empty)
    else unapplySeq(list.tail).map(list.head +: _)
}

val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty)))
val decomposed = myList match {
  case MyList(1, 2, _*) => "starting with 1, 2"
  case _ => "something else"
}

// custom return types for unapply
// isEmpty: Boolean, get: something.

abstract class Wrapper[T] {
  def isEmpty: Boolean

  def get: T
}

object PersonWrapper {
  def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
    def isEmpty = false

    def get = person.name
  }
}

val customReturnTypePatternTest = bob match {
  case PersonWrapper(n) => s"This person's name is $n"
  case _ => "An alien"
}

// More syntax - matching for lower case variables must use ``
val number0 = 0
val sum = 123
sum % 3 match {
  case x@`number0` => println(s"matched value for `number0`, value=$x")
  case x => println(s"matched value for the rest: $x")
}

// Options
def optionsPatternMatching(option: Option[Seq[Int]]): Unit = {
  option match {
    case Some(value) => println(s"I'm not an empty option. Value $value")
    case None => println("I'm an empty option")
  }
}

optionsPatternMatching(Some(Seq(1, 2, 3)))
optionsPatternMatching(None)

import scala.collection.mutable.Map

val str = "This is a string"
val charToIndices = (0 until str.length).foldLeft[Map[Char, Seq[Int]]](Map()) { (acc, index) =>
  val key = str(index)
  val value: Option[Seq[Int]] = acc.get(key)

 val entry = value match {
    case Some(indices) => key -> (index +: indices)
    case None => key -> Seq(index)
  }

  acc ++ Map(entry)
}

println(s"charToIndices=$charToIndices")

str.find(char => charToIndices(char).length == 0) match {
  case Some(c) => charToIndices(c)(0)
  case None => -1
}