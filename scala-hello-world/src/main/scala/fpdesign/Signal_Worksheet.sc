
class Signal[T](expr: () => T) {
  def apply(): T = ???

  private var myExpr: (() => T) = _ // _ means uninitialized
  private var myValue: T = _
  private var observers: Set[Signal[_]] = Set()
}

object Signal {
  private val caller = new StackableVariable[Signal[_]](NoSignal)

  //def apply[T](expr: => T): Signal[T] = new Signal[T](expr)
}
object NoSignal extends Signal[Nothing](???) {
  ???
}

class Var[T](expr: => T) extends Signal[T](expr) {
  def update(expr: => T) = ???
}

object Var {
  def apply[T](expr: => T): Var[T] = new Var[T](expr)
}

class StackableVariable[T](init: T) {
  private var values: List[T] = List(init)
  def value: T = values.head
  def withValue[R](newValue: T)(op: => R): R = {
    values = newValue :: values
    try op finally values = values.tail
  }
}

//class BankAccount {
//  val balance = Var[Int](0) // balance is a signal
//
//  def deposite(amount: Int): Unit = {
//    require(amount >= 0)
//    val current = balance()
//    balance() = current + amount
//  }
//
//  def withdraw(amount: Int): Unit = {
//    val current = balance()
//    if (current < amount) throw new IllegalArgumentException("Insufficient fund")
//    else balance() = current + amount
//  }
//}