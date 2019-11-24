// good resources on Monads (category theory)
// Brian Beckman: Don't fear the Monad: https://youtu.be/ZhuHCtR3xq8
// https://nunoalexandre.com/2016/10/13/the-monad-pattern
// Monoid meta laws:
// (Notations: @: be the operation to combine 2 elements; a: a type; f: a function; ->: returns)
// 1. associative: x @ (y @ z) = (x @ y) @ z
// 2. there exists a special element S such that: x @ S = S @ x
// Note that x @ y does NOT need to equal y @ x
// functions are monoids because they satify the 2 laws above, e.g. f: a -> a

// functions are just like data - think of a function lookup table.
// Monads (wikipedia: In functional programming, monads are a way to build computer programs by
// joining simple components in predictable and robust ways.)
// unit/return: (a) -> M a
// bind: (M a, f: a -> M b) -> M b

// Why use the Monad pattern:
// Compositionality: ability to compose complex functions from simple functions (mix and match
// a small set of small functions in arbitrary ways to build large complex systems).
// The Monad (the M in M a) hides complexity of side effects, concurrency, I/O, etc.
// Reduces code duplication/boilerplate, e.g. null checks

/**
 * Scala videos:
 * https://www.youtube.com/watch?v=X2D2QIKRfi8&list=PLLJhMcMkpqT2KFvAj0SsCRPTAa8Vsp1wM&index=10
 * https://www.youtube.com/watch?v=Mw_Jnn_Y5iA
 *
 * Monads capture policies for function application and composition, in map and flatMap. Examples:
 * Option monad makes the possibility of missing data explicit in the type system and hides the boilerplate of "if non-null" logic
 * List monad makes nondeterminism (variable # of elements) explicit in the type system and hides the boilerplate of loops
 * Future monad makes asynchronous computation explicit in the type system and hides the boilerplate of threading logic
 */
// To qualify as a Monad, a type must satisfy the following monadic laws:  (flatMap, aka bind, is the combining operation)
// Associativity: (m flatMap f) flatMap g ≡ m flatMap (x => f(x) flatMap g)
// Left unit/identity: unit(x) flatMap f ≡ f(x)
// Right unit/identity: m flatMap unit ≡ m
// In scala, bind is referred to as flatMap.  Every Scala Monhad type must define the following two functions
trait M[T] {
  def map[U](f: T => U): M[U] // the unit function is different for each type
  def flatMap[U](f: T => M[U]): M[U] // commonly referred to as "bind"
}

// Example1: Using Option Monad to remove boilerplate "if non-null" logic
case class Double(x: Int) {
  def compute: Option[Int] =
    if (x % 2 == 0) Some(x * 2)
    else None
}

case class Triple(x: Int) {
  def compute: Option[Int] =
    if (x % 3 == 0) Some(x * 3)
    else None
}

case class Increment(x: Int) {
  def compute: Option[Int] =
    if (x > 0) Some(x + 1)
    else None
}

// code will be much more readable if we use for expressions for nested flatMap and map
val nonValue =
  for {
    double <- Double(3).compute
    triple <- Triple(2).compute
    inc <- Increment(9).compute
  } yield double + triple + inc
val nonValueEquivalent =
  Double(3).compute.flatMap(double => {
    Triple(2).compute.flatMap(triple => {
      Increment(9).compute.map(inc => double + triple + inc)
    })
  })
val someValue =
  for {
    double <- Double(2).compute
    triple <- Triple(3).compute
    inc <- Increment(2).compute
  } yield double + triple + inc

/**
 * Sample implementations for map and flatMap for List, Option, and Try, to show concepts.  The actual
 * implementation is more complex for efficiency (to avoid non-tail recursive recursion)
 *
 * List and Option are both monads but not Try.
 */
// List
//abstract class List[+T](head: T, tail: List[T]) {
//  def map[U](f: T => U): List[U] = {
//    this match {
//      case Nil => Nil
//      case head :: tail => f(head) :: tail.map(f)
//    }
//  }
//
//  def flatMap[U](f: T => List[U]): List[U] = {
//    this match {
//      case Nil => Nil
//      case head :: tail => f(head) ++ tail.flatMap(f)
//    }
//  }
//}

// Option
//abstract class Option[+T] {
//  def flatMap[U](f: T => Option[U]): Option[U] = this match {
//    case None => None
//    case Some(x) => f(x)
//  }
//
//  def map[U](f: T => U): Option[U] = this match {
//    case None => None
//    case Some(x) => Some(f(x))
//  }
//}

// Try is not a Monad because the left identity/unit law does not hold.  However Try can still
// be used in for expressions if it implements map, flatMap, and withFilter because
// for expressions do not utilize the left unit law.
//abstract class Try[+T](x: T) {
//  def map[U](f: T => U): Try[U] = this match {
//    case Success(x) => Try[U][f(x)]
//    case fail: Failure => fail
//  }
//
//  def flatMap[U](f: T => Try[U]): Try[U] = this match {
//    case Success(x) => {
//      try f(x)
//      catch {
//        case NonFatal(ex) => Failure(ex)
//      }
//    }
//    case fail: Failure => fail
//  }
//}