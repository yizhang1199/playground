package exercises.implicits

object OrderingExample {
  implicit object ReverseStringOrdering extends Ordering[String] {
    override def compare(x: String, y: String): Int = y.compare(x)
  }

  implicit val StringLengthOrdering: Ordering[String] = Ordering.fromLessThan(_.length < _.length)
}