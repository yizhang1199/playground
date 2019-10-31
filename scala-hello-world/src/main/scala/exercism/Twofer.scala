package exercism

object Twofer {
  def twofer(name: String*): String = {
    val realName = name match {
      case "" +: _ => "you"
      case first +: _ => first
      case Seq() => "you"
    }

    s"One for $realName, one for me."
  }
}