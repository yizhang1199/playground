package exercism.hard

import scala.annotation.tailrec

/**
 * https://exercism.io/my/solutions/56809a2938ff4ff4a4fff1326ed7e675
 *
 * Write a function to solve alphametics puzzles.
 *
 * Alphametics is a puzzle where letters in words are replaced with numbers.
 *
 * For example SEND + MORE = MONEY:
 *
 * S E N D
 * M O R E +
 * -----------
 * M O N E Y
 *
 * Replacing these with valid numbers gives:
 *
 * 9 5 6 7
 * 1 0 8 5 +
 * -----------
 * 1 0 6 5 2
 *
 * This is correct because every letter is replaced by a different number and the words, translated into numbers,
 * then make a valid sum.
 *
 * Each letter must represent a different digit, and the leading digit of a multi-digit number must not be zero.
 */
object Alphametics {

  def solve(input: String): Option[Map[Char, Int]] = {
    val sanitized = input.replaceAll("[\\s]+", "").toUpperCase

    sanitized.split("==").toList match {
      case lhs :: sum :: Nil =>
        val addends = lhs.split("\\+").toList
        val alphabetAddition = AlphabetAddition(addends, sum)

        alphabetAddition.solve
      case _ => None
    }
  }
}

case class AlphabetAddition(addends: Seq[String], sum: String) {
  private val distinctAlphabets: Seq[Char] = (sum +: addends).flatten.distinct
  private val nonZeroAlphabets: Set[Char] = {
    var alphas = addends.collect {
      case addend: String if isMultiDigit(addend) => addend(0)
    }

    if (isMultiDigit(sum)) {
      alphas = sum(0) +: alphas // prepend is O(1)
    }

    alphas.toSet
  }

  def solve: Option[Map[Char, Int]] = {
    allSolutions().collectFirst {
      case solution: Map[Char, Int] if isValid(solution) => solution
    }
  }

  private def isValid(solution: Map[Char, Int]): Boolean = {
    def toInt(str: String): Int = {
      str.map(solution(_)).foldLeft(0) {
        (accumulator: Int, digit: Int) => accumulator * 10 + digit
      }
    }

    lazy val lhsSum: Int = addends.map(toInt).sum
    lazy val rhsSum: Int = toInt(sum)

    isComplete(solution) && lhsSum == rhsSum
  }

  private def isComplete(solution: Map[Char, Int]): Boolean = {
    distinctAlphabets.length == solution.size &&
      distinctAlphabets.filterNot(char => solution.keySet.contains(char)).isEmpty
  }

  private def getAllowedDigits(alphabet: Char): Seq[Int] = {
    val lowRange = if (nonZeroAlphabets.contains(alphabet)) 1 else 0

    lowRange to 9
  }

  private def merge(solutionsMerged: Seq[Map[Char, Int]],
                    solutions: Seq[Map[Char, Int]]): Seq[Map[Char, Int]] = {
    (solutionsMerged, solutions) match {
      case (solution1@Seq(_*), Seq()) =>
        solution1
      case (Seq(), solution2@Seq(_*)) =>
        solution2
      case _ =>
        for {
          s1 <- solutionsMerged
          s1Values = s1.values.toSet
          s2 <- solutions
          (key2, value2) <- s2 if !s1Values.contains(value2)
        } yield s1 + (key2 -> value2)
    }
  }

  @tailrec // TODO brute-force solution that's slow and takes up a lot of memory
  final def allSolutions(alphabets: Seq[Char] = distinctAlphabets,
                         solutionsMerged: Seq[Map[Char, Int]] = Seq()): Seq[Map[Char, Int]] = {
    alphabets match {
      case Seq() => solutionsMerged
      case alphabet :: tail =>
        val next = getAllowedDigits(alphabet).map(digit => Map(alphabet -> digit))
        val mergedAgain = merge(solutionsMerged, next)
        allSolutions(tail, mergedAgain)
    }
  }

  private def isMultiDigit(addend: String) = {
    addend.length > 1
  }
}
