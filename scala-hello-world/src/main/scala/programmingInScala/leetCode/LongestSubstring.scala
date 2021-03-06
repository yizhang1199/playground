package programmingInScala.leetCode

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * Given a string, find the length of the longest substring without repeating characters.
 *
 * Example 1:
 *
 * Input: "abcabcbb"
 * Output: 3
 * Explanation: The answer is "abc", with the length of 3.
 * Example 2:
 *
 * Input: "bbbbb"
 * Output: 1
 * Explanation: The answer is "b", with the length of 1.
 * Example 3:
 *
 * Input: "pwwkew"
 * Output: 3
 * Explanation: The answer is "wke", with the length of 3.
 * Note that the answer must be a substring, "pwke" is a subsequence and not a substring.
 */
object LongestSubstring extends App {

  def lengthOfLongestSubstring(s: String): Int = {
    require(s != null)
    val chars = s.toVector
    val length = s.length
    val charsProcessed = mutable.Map[Char, Int]()

    @tailrec
    def findLongest(start: Int = 0, current: Int = 0, longest: Int = 0): Int = {
      if (current >= length) {
        Math.max(longest, current - start)
      } else {
        val currentChar = chars(current)
        val duplicateIndex = charsProcessed.get(currentChar)
        charsProcessed += (currentChar -> current)
        var newStart = start
        if (duplicateIndex.isDefined && duplicateIndex.get >= start) newStart = duplicateIndex.get + 1
        findLongest(newStart, current + 1, Math.max(longest, current - start))
      }
    }

    findLongest()
  }

  private def findLongestIterative(chars: Vector[Char]): Int = {
    var longest, start, end = 0
    val charsProcessed = mutable.Map[Char, Int]()
    val length = chars.length
    while (continueProcessing(start, end, length, longest)) {
      val currentChar = chars(end)
      //println(s"currentChar=$currentChar, start=$start, end=$end, longest=$longest")
      if (charsProcessed.contains(currentChar)) {
        start = Math.max(charsProcessed(currentChar) + 1, start)
      }
      charsProcessed += (currentChar -> end)
      end = end + 1
      longest = Math.max(longest, end - start)
    }

    longest
  }

  private def continueProcessing(start: Int, end: Int, totalLength: Int, longestSoFar: Int): Boolean = {
    val remainingLength = totalLength - start
    start < totalLength && end < totalLength && longestSoFar < remainingLength
  }

  def lengthOfLongestSubstringFast(s: String): Int = {
    val seen = new Array[Boolean](128)
    var lastPos = 0
    var i = 0
    var res = 0
    while (i < s.length) {
      seen(s(i)) = true
      while (lastPos < s.length - 1 && !seen(s(lastPos + 1))) {
        lastPos += 1
        seen(s(lastPos)) = true
      }
      res = res.max(lastPos - i + 1)
      seen(s(i)) = false
      i += 1
      lastPos = lastPos.max(i)
    }
    res
  }

  println("abcabcbb=" + lengthOfLongestSubstring("abcabcbb"))

  assert(3 == lengthOfLongestSubstring("abcabcbb"))
  assert(1 == lengthOfLongestSubstring("bbbbb"))
  assert(3 == lengthOfLongestSubstring("pwwkew"))
  assert(3 == lengthOfLongestSubstring("dvdf"))
  assert(5 == lengthOfLongestSubstring("anviaj"))
  assert(6 == lengthOfLongestSubstring("abcdef"))
  assert(0 == lengthOfLongestSubstring(""))
}
