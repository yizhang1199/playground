package exercism.medium

import org.scalatest.{FunSuite, Matchers}

/** @version 1.1.0 */
class SpiralMatrixSpec extends FunSuite with Matchers {

  test("empty spiral") {
    SpiralMatrix.spiralMatrix(0) should be(List())
  }

  test("trivial spiral") {
    
    SpiralMatrix.spiralMatrix(1) should be(List(List(1)))
  }

  test("spiral of size 2") {
    
    SpiralMatrix.spiralMatrix(2) should be(List(List(1, 2),
      List(4, 3)))
  }

  test("spiral of size 3") {
    
    SpiralMatrix.spiralMatrix(3) should be(
      List(List(1, 2, 3),
        List(8, 9, 4),
        List(7, 6, 5)))
  }

  test("spiral of size 4") {
    
    SpiralMatrix.spiralMatrix(4) should be(
      List(List(1, 2, 3, 4),
        List(12, 13, 14, 5),
        List(11, 16, 15, 6),
        List(10, 9, 8, 7)))
  }

  test("spiral of size 5") {
    
    SpiralMatrix.spiralMatrix(5) should be(
      List(List(1, 2, 3, 4, 5),
        List(16, 17, 18, 19, 6),
        List(15, 24, 25, 20, 7),
        List(14, 23, 22, 21, 8),
        List(13, 12, 11, 10, 9)))
  }

  test("spiral of size 10") {

    //println(SpiralMatrix.spiralMatrix(10) mkString "\n")
    SpiralMatrix.spiralMatrix(10) should be(
      List(
        List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        List(36, 37, 38, 39, 40, 41, 42, 43, 44, 11),
        List(35, 64, 65, 66, 67, 68, 69, 70, 45, 12),
        List(34, 63, 84, 85, 86, 87, 88, 71, 46, 13),
        List(33, 62, 83, 96, 97, 98, 89, 72, 47, 14),
        List(32, 61, 82, 95, 100, 99, 90, 73, 48, 15),
        List(31, 60, 81, 94, 93, 92, 91, 74, 49, 16),
        List(30, 59, 80, 79, 78, 77, 76, 75, 50, 17),
        List(29, 58, 57, 56, 55, 54, 53, 52, 51, 18),
        List(28, 27, 26, 25, 24, 23, 22, 21, 20, 19)))
  }
}
