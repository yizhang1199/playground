package fpprinciple

object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello World!")
    val x: IntSet = new NonEmpty(2, Empty, Empty);
    val y: IntSet = x include 5
    val z: IntSet = new NonEmpty(7, Empty, Empty);

    println(x)
    println(y)
    println(z union y)
    println(y union z)

    val str = "I'm a string."
    str.grouped(3).foreach(println)
    println("groupBy:")
    str.toSeq.groupBy(identity).foreach(println)
  }
}

abstract class IntSet {
  def union(that: IntSet): IntSet
  def include(value: Int): IntSet
  def contains(value: Int): Boolean
}

// defines a singleton object
object Empty extends IntSet {
  def contains(v: Int) = false
  def include(v: Int) = new NonEmpty(v, Empty, Empty)

  override def union(that: IntSet): IntSet = that

  override def toString: String = "_"
}

class NonEmpty(n: Int, l: IntSet, r: IntSet) extends IntSet {
  require(l != null, "left tree must not be null")
  require(r != null, "right tree must not be null")
  def node = n
  def left = l
  def right = r

  override def union(obj: IntSet): IntSet = {
    if (obj == null || obj == Empty) {
      this
    }
    val that:NonEmpty = obj.asInstanceOf[NonEmpty]
    if (this.node == that.node) {
      new NonEmpty(this.node, this.left.union(that.left), this.right.union(that.right))
    } else if (this.node < that.node) {
      new NonEmpty(this.node, this.left, this.right.union(that))
    } else {
      new NonEmpty(this.node, this.left.union(that), this.right)
    }
  }

  override def include(value: Int): IntSet = {
    if (value < node) new NonEmpty(node, left include value, right)
    else if (value > node) new NonEmpty(node, left, right include value)
    else this
  }

  override def contains(value: Int): Boolean = {
    if (value < node) left.contains(value)
    else if (value > node) right.contains(value)
    true
  }

  override def toString: String = "{" + left + node + right + "}"
}