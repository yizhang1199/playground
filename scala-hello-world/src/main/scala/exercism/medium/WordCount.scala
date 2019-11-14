package exercism.medium

/**
 * https://exercism.io/my/solutions/b97a21682a914fa1b12abaacc18a44a0
 *
 * Given a phrase, count the occurrences of each word in that phrase.
 *
 * For the purposes of this exercise you can expect that a word will always be one of:
 *
 * A number composed of one or more ASCII digits (ie "0" or "1234") OR
 * A simple word composed of one or more ASCII letters (ie "a" or "they") OR
 * A contraction of two simple words joined by a single apostrophe (ie "it's" or "they're")
 *
 * When counting words you can assume the following rules:
 *
 * The count is case insensitive (ie "You", "you", and "YOU" are 3 uses of the same word)
 * The count is unordered; the tests will ignore how words and counts are ordered
 * Other than the apostrophe in a contraction all forms of punctuation are ignored
 * The words can be separated by any form of whitespace (ie "\t", "\n", " ")
 *
 * For example, for the phrase "That's the password: 'PASSWORD 123'!", cried the Special Agent.\nSo I fled. the count would be:
 *
 * that's: 1
 * the: 2
 * password: 2
 * 123: 1
 * cried: 1
 * special: 1
 * agent: 1
 * so: 1
 * i: 1
 * fled: 1
 *
 * @param words a list of words to count, must not be null
 */
case class WordCount(words: String) {
  require(words != null)
  private val apostrophe = "'"
  private val regex = "[\\s_\\W&&[^']]+"

  def countWords(): Map[String, Int] = {
    words.trim().split(regex)
      .map {
        s => {
          val word = s.collect {
            case char: Char if isLetterOrDigitOrApostrophe(char) => char
          }
          word.stripPrefix(apostrophe).stripSuffix(apostrophe).toLowerCase
        }
      }
      .foldLeft(Map[String, Int]()) {
        (acc, element) =>
          acc.get(element) match {
            case Some(count) => acc + (element -> (count + 1))
            case None => acc + (element -> 1)
          }
      }
  }

  private def isLetterOrDigitOrApostrophe(char: Char): Boolean = char.isLetterOrDigit || char == apostrophe.charAt(0)
}
