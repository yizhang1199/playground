package exercism

import scala.annotation.tailrec

object MatchingBrackets {
  private val rightToLeftMapping = Map(')' -> '(', ']' -> '[', '}' -> '{')
  private val leftSides = rightToLeftMapping.values.toSet[Char]

  def isPaired(input: String): Boolean = {
    if (input.length == 0) true
    else isPaired(List(), input.toList)
  }

  @tailrec
  private def isPaired(processedStack: List[Char], remainder: List[Char]): Boolean =
    remainder match {
      case head :: tail if leftSides.contains(head) =>
        isPaired(head :: processedStack, tail)
      case head :: tail if rightToLeftMapping.contains(head) && // found a right side
        processedStack.nonEmpty &&
        processedStack.head == rightToLeftMapping(head) =>
        isPaired(processedStack.tail, tail)
      case head :: _ if rightToLeftMapping.contains(head) => false
      case _ :: tail => isPaired(processedStack, tail)
      case List() => processedStack.isEmpty
    }

  // Another solution from the community -- shows more ways to pattern match
  @tailrec
  private def usingRecursion(brackets: List[Char],
                             stack: List[Char] = List()): Boolean =
    (brackets, stack) match {
      case (Nil, Nil) => true
      case (('{' | '[' | '(') :: xs, s) =>
        usingRecursion(xs, brackets.head +: s)
      case ('}' :: xs, '{' :: ys) => usingRecursion(xs, ys)
      case (']' :: xs, '[' :: ys) => usingRecursion(xs, ys)
      case (')' :: xs, '(' :: ys) => usingRecursion(xs, ys)
      case _ => false
    }
}
