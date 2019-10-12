import scala.collection.mutable.ArrayBuffer
// A Scala trait with only abstract members (methods) translates directly to a Java interface
abstract class IntQueue {
  def get(): Int
  def put(x: Int)
}

class BasicIntQueue extends IntQueue {
  private val buf = new ArrayBuffer[Int]
  def get() = buf.remove(0)
  def put(x: Int) = { buf += x }
}

trait Doubling extends IntQueue {
  abstract override def put(x: Int): Unit = {
    super.put(x * 2)
  }
}

trait Incrementing extends IntQueue {
  abstract override def put(x: Int) = { super.put(x + 1) }
}

trait Filtering extends IntQueue {
  abstract override def put(x: Int): Unit = {
    if (x >= 0 ) super.put(x)
  }
}
// Order of mixins matters.  When you call a method on a class with mixins, the method in the
// trait furthest to the right is called first. If that method calls super, it invokes the
// method in the next trait to its left, and so on.
val filterThenDouble = new BasicIntQueue with Doubling with Filtering
for (i <- -1 to 2) filterThenDouble.put(i)
for (i <- -1 to 1) print(" " + filterThenDouble.get)

val incrementThenFilter = new BasicIntQueue with Filtering with Incrementing
for (i <- -1 to 2) incrementThenFilter.put(i)
for (i <- -1 to 2) print(" " + incrementThenFilter.get)

trait Noisy {
  def speak() = {
    println("noisy")
  }
}

trait Chatty {
  def speak(): Unit = {
    println("chatty")
  }
}

// this results in a runtime error as NoisyPerson inherits speak() from both Noisy and Chatty
class NoisyPerson extends Noisy with Chatty {
  def greet() = {
    println("Hello")
  }
}

val p = new NoisyPerson
p.speak()

