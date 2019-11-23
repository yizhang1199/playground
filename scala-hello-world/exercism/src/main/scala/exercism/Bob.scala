package exercism

/**
 * https://exercism.io/my/solutions/e30c473c4eba4fb5942bcd60be426038
 *
 * Bob is a lackadaisical teenager. In conversation, his responses are very limited.
 *
 * Bob answers 'Sure.' if you ask him a question, such as "How are you?".
 *
 * He answers 'Whoa, chill out!' if you YELL AT HIM (in all capitals).
 *
 * He answers 'Calm down, I know what I'm doing!' if you yell a question at him.
 *
 * He says 'Fine. Be that way!' if you address him without actually saying anything.
 *
 * He answers 'Whatever.' to anything else.
 *
 * Bob's conversational partner is a purist when it comes to written communication and
 * always follows normal rules regarding sentence punctuation in English.
 */
object Bob {
  def response(statement: String): String = statement match {
    case yellingQuestion() => "Calm down, I know what I'm doing!"
    case yelling() => "Whoa, chill out!"
    case question() => "Sure."
    case silence() => "Fine. Be that way!"
    case _ => "Whatever."
  }
}

private object yellingQuestion {
  def unapply(input: String): Boolean = Utils.isQuestion(input) && Utils.isYelling(input)
}

private object yelling {
  def unapply(input: String): Boolean = Utils.isYelling(input)
}

private object question {
  def unapply(input: String): Boolean = Utils.isQuestion(input)
}

private object silence {
  def unapply(input: String): Boolean = input.trim.isEmpty
}

private object Utils {
  def isQuestion(input: String) = input.trim.endsWith("?")

  def isYelling(input: String) = input.exists(char => char.isLetter) && input.toUpperCase == input
}