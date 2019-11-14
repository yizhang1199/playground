package exercism

object Twofer {
  def twofer(name: String = "you"): String = {
    require(name.nonEmpty)
    s"One for $name, one for me."
  }
}