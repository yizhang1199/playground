package programmingInScala.leetCode

import scala.annotation.tailrec

object MinimumWindowSubstring extends App {

  assert("BANC" == minWindow("ADOBECODEBANC", "ABC"))
  assert("AB" == minWindow("AAB", "AB"))
  assert("" == minWindow("AABIWOQO", "ADB"))

  def minWindow(s: String, t: String): String = {
    require(s != null && t != null)

    (s, t) match {
      case ("", _) => ""
      case (_, "") => ""
      case (s, t) if t.length > s.length => ""
      case (s, t) => findMinWindow(s, t)
    }
  }

  private def findMinWindow(source: String, target: String): String = {
    var left = 0
    var right = 0
    var minWindow = ""

    while (left < source.length && right < source.length) {

      if (isValid(source, left, right, target)) {
        minWindow = selectMinWindow(minWindow, source.substring(left, right + 1))
        val window2 = slideLeft(source, left, right, target)
        minWindow = selectMinWindow(minWindow, window2)
        left = window2.length - 1
      }

      right = right + 1
    }

    minWindow
  }

  @tailrec
  private def slideLeft(source: String, left: Int, right: Int, target: String): String = {
    if (isValid(source, left, right, target)) {
      slideLeft(source, left + 1, right, target)
    } else {
      source.substring(left - 1, right + 1)
    }
  }

  private def selectMinWindow(minWindow: String, newWindow: String): String = {
    (minWindow.length, newWindow.length) match {
      case (_, 0) => minWindow
      case (0, _) => newWindow
      case (x, y) if x > y => newWindow
      case _ => minWindow
    }
  }

  private def isValid(source: String, startIndex: Int, endIndex: Int, target: String): Boolean = {
    val windowLength = endIndex - startIndex + 1
    var isValid = windowLength >= target.length

    if (isValid) {
      var charToCount = target.groupMapReduce[Char, Int] { char => char } { _ => 1 } { (x, y) => x + y }

      (startIndex to endIndex).foreach { index =>
        val sourceChar = source(index)
        charToCount.get(sourceChar) match {
          case Some(count) => charToCount = charToCount.updated(sourceChar, count - 1)
          case None =>
        }
      }

      isValid = !charToCount.valuesIterator.exists(count => count > 0)
    }

    isValid
  }

}
