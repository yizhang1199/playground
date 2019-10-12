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
// Monads (wikipedia: In functional programming, monads are a way to build computer programs by joining simple components in predictable and robust ways.)
// unit/return: (a) -> M a
// bind: (M a, f: a -> M b) -> M b

// To qualify as a Monad, a type must satisfy the following monadic laws:  (in this case the flatMap is the combining operation)
// Associativity: (m flatMap f) flatMap g ≡ m flatMap (x => f(x) flatMap g)
// Left unit/identity: unit(x) flatMap f ≡ f(x)
// Right unit/identity: m flatMap unit ≡ m

// Why use the Monad pattern:
// Compositionality: ability to compose complex functions from simple functions (mix and match a small set of small functions in arbitrary ways to build large complex systems).  The Monad (the M in M a) hides complexity of side effects, concurrency, I/O, etc.
// Reduces code duplication/bolier plate, e.g. null checks

/**
 * Scala specific
 * Scala video: https://www.youtube.com/watch?v=X2D2QIKRfi8&list=PLLJhMcMkpqT2KFvAj0SsCRPTAa8Vsp1wM&index=10
 */
//
// In scala, bind is referred to as flatMap.  Every Scala Monhad type must define the following two functions
trait M[T] {
  def map[U](f: T => U) : M[U] // the unit function is different for each type
  def flatMap[U](f: T => M[U]) : M[U]  // commonly referred to as "bind"
}

// Sample implementations for map and flatMap for List
// The actual implementation is more complex for efficiency (to avoid non-tail recursive recursion)
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

// Sample implementation for map and flatMap on Option
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

// Try is not a Monad because the left unit law does not hold.  However Try can still
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