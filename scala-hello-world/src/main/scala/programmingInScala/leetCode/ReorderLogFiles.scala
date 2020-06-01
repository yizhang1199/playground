package programmingInScala.leetCode

object ReorderLogFiles extends App {

  assert(reorderLogFiles(Array("1 n u", "r 527", "j 893", "6 14", "6 82")) sameElements Array("1 n u", "r 527", "j 893", "6 14", "6 82"))

  val input = Array("dig1 8 1 5 1", "let1 art can", "dig2 3 6", "let2 own kit dig", "let3 art zero")
  val expected = Array("let1 art can", "let3 art zero", "let2 own kit dig", "dig1 8 1 5 1", "dig2 3 6")
  assert(reorderLogFiles(input) sameElements expected)

  def reorderLogFiles(logs: Array[String]): Array[String] = {
    require(logs != null)

    logs match {
      case Array() => Array()
      case _ => solve(logs.toSeq).toArray
    }
  }

  private def solve(logs: Seq[String]): Seq[String] = {
    val result = logs.sorted(CustomeLogOrdering)
    println(s"result=$result")
    result
  }

  implicit object CustomeLogOrdering extends Ordering[String] {
    override def compare(log1: String, log2: String): Int = (log1, log2) match {
      case (DigitLog(), DigitLog()) => 1 // TODO relies on implementation of sorted, which currently starts with the last element
      case (_, DigitLog()) => -1
      case (DigitLog(), _) => 1
      case _ => compareLetterLogs(log1, log2)
    }
  }

  private def compareLetterLogs(log1: String, log2: String): Int = {
    val elements1 = log1.split(" ", 2)
    val elements2 = log2.split(" ", 2)

    val firstResult = elements1(1).compare(elements2(1))

    if (firstResult == 0) {
      elements1(0).compare(elements2(0))
    } else {
      firstResult
    }
  }

  private object DigitLog {
    def unapply(log: String): Boolean = {
      val elements = log.split(" ")

      elements(1).matches("[\\d]+")
    }
  }

}
