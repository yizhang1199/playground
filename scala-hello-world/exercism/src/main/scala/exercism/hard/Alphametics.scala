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

case class AlphabetAddition(addends: List[String], sum: String) {
  private lazy val distinctAlphabets: List[Char] = (sum +: addends).flatten.distinct
  private lazy val nonZeroAlphabets: Set[Char] = {
    var alphas = addends.collect {
      case addend: String if isMultiDigit(addend) => addend(0)
    }

    if (isMultiDigit(sum)) {
      alphas = sum(0) :: alphas // cons is O(1)
    }

    alphas.toSet // Use Set for O(1) contains
  }

  def solve: Option[Map[Char, Int]] = {
    allSolutions().collectFirst {
      case solution: Map[Char, Int] if isValid(solution) => solution
    }
  }

  private def isValid(solution: Map[Char, Int]): Boolean = {
    def toInt(str: String): Int = {
      str.map(solution).foldLeft(0) {
        (accumulator: Int, digit: Int) => accumulator * 10 + digit
      }
    }

    lazy val lhsSum: Int = addends.map(toInt).sum
    lazy val rhsSum: Int = toInt(sum)

    isComplete(solution) && lhsSum == rhsSum
  }

  private def isComplete(solution: Map[Char, Int]): Boolean = {
    distinctAlphabets.length == solution.size &&
      distinctAlphabets.forall(char => solution.keySet.contains(char))
  }

  private def getAllowedDigits(alphabet: Char): Seq[Int] = {
    val lowRange = if (nonZeroAlphabets.contains(alphabet)) 1 else 0

    lowRange to 9
  }

  private def merge(solutionsMerged: Seq[Map[Char, Int]],
                    solutionsToBeMerged: Seq[Map[Char, Int]]): Seq[Map[Char, Int]] = {
    (solutionsMerged, solutionsToBeMerged) match {
      case (solution1@Seq(_*), Seq()) =>
        solution1
      case (Seq(), solution2@Seq(_*)) =>
        solution2
      case _ =>
        for {
          s1 <- solutionsMerged
          s1Values = s1.values.toSet
          s2 <- solutionsToBeMerged
          (key2, value2) <- s2 if !s1Values.contains(value2)
        } yield s1 + (key2 -> value2)
    }
  }

  @tailrec // TODO brute-force solution that generates all possible solutions. Slow and takes up a lot of memory
  final def allSolutions(alphabets: Seq[Char] = distinctAlphabets,
                         solutionsMerged: Seq[Map[Char, Int]] = Seq()): Seq[Map[Char, Int]] = {
    alphabets match {
      case Seq() => solutionsMerged
      case alphabet :: tail =>
        val nextSolution = getAllowedDigits(alphabet).map(digit => Map(alphabet -> digit))
        val mergedAgain = merge(solutionsMerged, nextSolution)
        allSolutions(tail, mergedAgain)
    }
  }

  private def isMultiDigit(addend: String) = {
    addend.length > 1
  }

  // ----------------------------------------
  // Highlights from community solutions:
  // ----------------------------------------
  /**
   * https://exercism.io/tracks/scala/exercises/alphametics/solutions/ebd5a4042382492093d0093913ab52e0
   * Same brute force approach but much better use of combinations & permutations to generate all solutions.
   * Still takes seconds but using the library methods are 2x faster than my solution.  Example:
   */
  private def allSolutionsUsingCombinationsAndPermutations: Seq[Map[Char, Int]] = {
    def zeroRule(numbers: Seq[Int], letters: Seq[Char]): Boolean = {
      val zeroRuleBroken = (numbers contains 0) &&
        nonZeroAlphabets.contains(letters(numbers.indexOf(0)))
      !zeroRuleBroken
    }

    val combinations = for {
      numbers <- (0 to 9).combinations(distinctAlphabets.size)
      letters <- distinctAlphabets.permutations if zeroRule(numbers, letters)
    } yield (letters zip numbers).toMap

    combinations.toSeq
  }
}