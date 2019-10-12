// https://www.youtube.com/watch?v=hOPvt9LaZ_8&list=PLTeAcbOFvqaGtSMysJzzcYhrZyzpKyvKN&index=5
// http://scalagwt.github.io/samples/Mnemonics.html
// https://github.com/frankh/coursera-scala

import scala.io.Source

/* read a file of words */
val in = Source.fromURL("https://raw.githubusercontent.com/frankh/coursera-scala/master/forcomp/src/main/resources/forcomp/linuxwords.txt")

/* create a list and filter all words where *all* their characters are not letters (like dashes) */
//val words : List[String] = in.getLines.toList filter (word => word forall (chr => chr.isLetter))
val words : List[String] = in.getLines.toList

val mnemonics: Map[Char, String] = Map('2' -> "ABC", '3' -> "DEF", '4' -> "GHI",
  '5' -> "JKL", '6' -> "MNO", '7' -> "PQRS", '8' -> "TUV", '9' -> "WXYZ")

val mnemonicsWithDefaults = mnemonics withDefaultValue ""

// maps an alphabet to digit, e.g. A -> 2
val charCodeWithDefault: Map[Char, Char] = {
  val result: List[(Char, Char)] = for {
    (digit, chars) <- mnemonics.toList
    char: Char <- chars
  } yield (char -> digit)
  result.toMap withDefaultValue '-'
}

// from alphabet to digit, e.g. A -> 2
private val charCode: Map[Char, Char] =
  for ((digit, chars) <- mnemonics; char: Char <- chars) yield (char -> digit)

// maps alpha strings to digits
def wordCode(str: String): String = str.toUpperCase map charCodeWithDefault
//for (char <- str) yield charCode(char.toUpper)

// maps a numeric string to a sequence of words that represent it,
// e.g. 5282 -> List("Java", "Kata", "Lava", ...)
def numberToRandomWords(numberString: String): Seq[String] = {
  (numberString foldLeft List[String](""))((list, digit) => {
    val mappedChars : String = mnemonicsWithDefaults(digit)
    for {
      str <- list
      char <- mappedChars
    } yield str + char
  })
}

val wordsFor5282 = numberToRandomWords("5282")

// map number to 1 or more words in the "words" list.
val numberToWordsMap: Map[String, Seq[String]] = (words groupBy wordCode) withDefaultValue List()
// return all ways to encode a number, using words in "words"
def encode(number: String): Set[Seq[String]] = {
  if (number.isEmpty) Set(List())
  else
    for {
      split: Int <- (1 to number.length).toSet
      word: String <- numberToWordsMap(number take split)
      rest: List[String] <- encode(number drop split)
    } yield word :: rest
}

val numberToPhrases : Set[String] = encode("7225247386") map (_ mkString " ")
val numberToPhrasesString = numberToPhrases mkString "\n"