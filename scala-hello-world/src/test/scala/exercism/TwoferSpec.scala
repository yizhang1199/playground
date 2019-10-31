package exercism

import org.scalatest.WordSpec
import org.scalatest.Matchers._

class TwoferSpec extends WordSpec {
  "Twofer" should {
    "use name when given" in {
      val result = Twofer.twofer("Kitty")
      result should be ("One for Kitty, one for me.")
    }

    "use you when name is empty" in {
      val result = Twofer.twofer("")
      result should be ("One for you, one for me.")
    }

    "use you when name is not given" in {
      val result = Twofer.twofer()
      result should be ("One for you, one for me.")
    }
  }
}