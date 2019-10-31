package exercism

import scala.annotation.tailrec

object MatchingBrackets {
  private val matchingPairs = Map(')' -> '(', ']' -> '[', '}' -> '{')
  private val leftSides = Set('(', '[', '{')

  def isPaired(input: String): Boolean = {
    if (input.length == 0) true
    else isPaired(List(), input.toList)
  }

  @tailrec
  private def isPaired(processedReversed: List[Char], rest: List[Char]): Boolean =
    rest match {
      case head :: tail if leftSides.contains(head) =>
        isPaired(head :: processedReversed, tail)
      case head :: tail if matchingPairs.contains(head) && // found a right side
        processedReversed.nonEmpty &&
        processedReversed.head == matchingPairs(head) =>
        isPaired(processedReversed.tail, tail)
      case head :: _ if matchingPairs.contains(head) =>
        false
      case _ :: tail => isPaired(processedReversed, tail)
      case List() => processedReversed.isEmpty
    }
}
