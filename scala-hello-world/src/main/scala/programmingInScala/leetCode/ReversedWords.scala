package programmingInScala.leetCode

object ReversedWords extends App {

  println("result=" + reverseWords(Seq('A', ' ', 'l', 'a', 'z', 'y', ' ', 'f', 'o', 'x')))

  def doWork(input: Seq[Char]): Seq[Char] = {
    println(s"input=$input")
    val words = input.mkString("").split(" ").toSeq
    println(s"input.toString()=${input.toString()}, words=$words")
    val reversedWords =  words.reverse
    var firstTime = true

    println(s"reversedWords=$reversedWords")
    val reversedChars = reversedWords.flatMap { word: String =>
      if (firstTime) {
        firstTime = false
        word.toIndexedSeq
      } else {
        ' ' +: word.toIndexedSeq // O(1)
      }
    }

    reversedChars
  }

  def reverseWords(input: Seq[Char]): Seq[Char] = {
    input match {
      case null | Seq() | Seq(_) => input
      case _ => doWork(input)
    }
  }

  /**
   * /*
   * * Reverse the order of the words in input.
   * *
   * * Input:  ['A', ' ', 'l', 'a', 'z', 'y', ' ', 'f', 'o', 'x']
   * * Output: ['f', 'o', 'x', ' ', 'l', 'a', 'z', 'y', ' ', 'A']
   **/
   * //public byte[] reverseWords(byte[] input) {
   *
   * //}
   *
   * def reverseWords(input: Seq[Char]): Seq[Char] = {
   * input match {
   * case null | Seq() | Seq(head) => input
   * case _ => doWork(input)
   * }
   * }
   *
   * private def doWork(input: Seq[Char]): Seq[Char] = {
   *
   * val words = input.toString.split(" ") // Seq[String] = Seq("A", "lazy", "fox"]
   * val reversedWords = words.reverse // Seq("fox", "lazy", "A")
   *
   * var firstTime = true
   * val reversedChars = reversedWords.flatMap { word => // Seq[Char]
   * if (firstTime) {
   *             word.toChar
   * firstTime = false
   * } else {
   * Char(' ') +: word.toChar // O(1)
   * }
   * }
   *
   * reversedChars
   * }
   */
}
