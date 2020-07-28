package programmingInScala.leetCode.trees

import scala.collection.mutable

/**
 * Given two words (beginWord and endWord), and a dictionary's word list, find the length of shortest transformation sequence from beginWord to endWord, such that:
 *
 * Only one letter can be changed at a time.
 * Each transformed word must exist in the word list.
 *
 * Note:
 * Return 0 if there is no such transformation sequence.
 * All words have the same length.
 * All words contain only lowercase alphabetic characters.
 * You may assume no duplicates in the word list.
 * You may assume beginWord and endWord are non-empty and are not the same.
 *
 * Example 1:
 * Input:
 * beginWord = "hit",
 * endWord = "cog",
 * wordList = ["hot","dot","dog","lot","log","cog"]
 *
 * Output: 5
 *
 * Explanation: As one shortest transformation is "hit" -> "hot" -> "dot" -> "dog" -> "cog",
 * return its length 5.
 *
 * Example 2:
 * Input:
 * beginWord = "hit"
 * endWord = "cog"
 * wordList = ["hot","dot","dog","lot","log"]
 *
 * Output: 0
 *
 * Explanation: The endWord "cog" is not in wordList, therefore no possible transformation.
 */
object WordLadder extends App {

  assert(5 == ladderLength("hit", "cog", List("hot","dot","dog","lot","log","cog")))
  assert(0 == ladderLength("hit", "cog", List("hot","dot","dog","lot","log","bog")))

  def ladderLength(beginWord: String, endWord: String, wordList: List[String]): Int = {
    require(beginWord != null)
    require(endWord != null)
    require(wordList != null)

    (beginWord, endWord, wordList) match {
      case (_, _, List()) => 0
      case (b, e, _) if b == e => 0
      case (b, e, _) if b.length != e.length => 0
      case _ => findLadderLength(beginWord, endWord, wordList.toSet)
    }
  }

  private def findLadderLength(beginWord: String, endWord: String, dictionary: Set[String]): Int = {
    val queue = mutable.Queue[Set[String]](Set(beginWord))
    var depth = 0
    var found = false
    val visitedDictionary: mutable.Set[String] = mutable.Set()

    while (queue.nonEmpty) {
      depth = depth + 1
      val sameLevelWords = queue.dequeue

      if (sameLevelWords.contains(endWord)) {
        found = true
      } else {
        visitedDictionary.addAll(sameLevelWords)
        val nextLevelWords = sameLevelWords.flatMap(findNextWords(_, dictionary.diff(visitedDictionary)))
        if (nextLevelWords.nonEmpty) {
          queue.enqueue(nextLevelWords)
        }
      }
    }

    if (found) depth else 0
  }

  private def findNextWords(word: String, dictionary: Set[String]): Set[String] = {
    dictionary.foldLeft[Set[String]](Set()) { (acc, dictionaryWord) =>
      if (findDifferenceCount(dictionaryWord, word) == 1) {
        acc ++ Set(dictionaryWord)
      } else {
        acc
      }
    }
  }

  private def findDifferenceCount(word1: String, word2: String): Int = {
    (0 until word1.length).foldLeft(0) { (count, index) =>
      if(word1.charAt(index) != word2.charAt(index)) {
        count + 1
      } else {
        count
      }
    }
  }
}
