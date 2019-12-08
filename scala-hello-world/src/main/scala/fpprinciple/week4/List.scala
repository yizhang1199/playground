package fpprinciple.week4

object ListTest {
  def main(args: Array[String]): Unit = {
    val ints: List[Int] = new LinkedList[Int](4, Nil)
    ints.append(5)
    ints.append(6)
    ints.append(7)

    ints.nth(0)
  }
}

// +T makes List[T] covariant: if U is a super type of T, then List[U] is a super type of List[T]. Covariants can only appear as return types.
// -T makes List[T] contravariant: if U is a super type of T, then List[T] is a super type of List[U].  Contravariants can only appear as arguements in a method/function
// T makes List[T] invariant: if U is a super type of T, then List[U] is neither a super type or sub type of List[T].  Invariant types can appear as both arguements and return types.
trait List[+T] {
  def isEmpty: Boolean

  def head: T

  def tail: List[T]

  // Covariants can only appear as return types.  To get around this, a super type of T is used, e.g. U >: T
  def prepend[U >: T](element: U): List[U] = new LinkedList[U](element, this)

  def ::[U >: T](element: U): List[U] = prepend(element)

  def append[U >: T](element: U): List[U] = {
    if (this.isEmpty) {
      new LinkedList[U](element, Nil)
    } else {
      new LinkedList[U](this.head, tail.append(element))
    }
  }

  def nth(i: Int): T
}

object Nil extends List[Nothing] {
  override def isEmpty: Boolean = true

  override def head: Nothing = throw new NoSuchElementException("Nil.head")

  override def tail: List[Nothing] = throw new NoSuchElementException("Nil.tail")

  override def nth(i: Int): Nothing = throw new NoSuchElementException("Nil.nth")

  override def toString: String = "Nil"
}

class LinkedList[T](val head: T, val tail: List[T]) extends List[T] {
  require(head != null, "head cannot be null")
  require(tail != null, "next cannot be null")

  override def isEmpty: Boolean = false

  override def nth(i: Int): T = {
    if (i < 0) throw new IndexOutOfBoundsException("")
    else if (i == 0) head
    else tail.nth(i - 1)
  }

  override def toString: String = "head=" + head + ", next=" + tail
}