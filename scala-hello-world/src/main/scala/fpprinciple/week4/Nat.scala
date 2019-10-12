package fpprinciple.week4

// Natural numbers: the positive integers (whole numbers) 1, 2, 3, etc., and sometimes zero as well.
trait Nat {
  def isZero: Boolean
  def predecessor(): Nat
  def successor: Nat = new Successor(this)
  def +(other: Nat): Nat
  def -(other: Nat): Nat
}

object zero extends Nat {
  override def isZero: Boolean = true

  override def predecessor: Nat = throw new NoSuchElementException("Zero.predecessor")

  override def +(other: Nat): Nat = other

  override def -(other: Nat): Nat = if (other.isZero) this else throw new UnsupportedOperationException("Zero.subtract")

  override def toString: String = "0"
}

class Successor(pred: Nat) extends Nat {
  require(pred != null, "Input natural number cannot be null")

  override def isZero: Boolean = false

  override def predecessor(): Nat = pred

  override def +(other: Nat): Nat = {
    new Successor(pred + other)
  }

  override def -(other: Nat): Nat = {
    if (other.isZero) this else pred - other.predecessor()
  }

  //  override def +(other: Nat): Nat = {
  //    apply(n => n.successor)(other)
  //  }

  //  override def -(other: Nat): Nat = {
  //    apply(n => n.predecessor)(other)
  //  }

  override def toString: String = String.valueOf(value)

  private def value: Int = {
    var v = 1
    var n = pred
    while (!n.isZero) {
      v += 1
      n = n.predecessor
    }
    v
  }

  private def apply(f: Nat => Nat)(o: Nat) = {
    var n: Nat = this
    var other: Nat = o
    while (!other.isZero) {
      n = f(n)
      other = other.predecessor()
    }
    n
  }
}
