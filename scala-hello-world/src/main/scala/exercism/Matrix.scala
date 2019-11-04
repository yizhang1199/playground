package exercism

/**
 * https://exercism.io/my/solutions/81bfeb576250414aa314777fc8e5dcc4
 *
 * Given a string representing a matrix of numbers, return the rows and columns of that matrix.
 *
 * So given a string with embedded newlines like:
 *
 * 9 8 7
 * 5 3 2
 * 6 6 7
 *
 * representing this matrix:
 *
 * 1  2  3
 * |---------
 * 1 | 9  8  7
 * 2 | 5  3  2
 * 3 | 6  6  7
 *
 * your code should be able to spit out:
 *
 * A list of the rows, reading each row left-to-right while moving top-to-bottom across the rows,
 * A list of the columns, reading each column top-to-bottom while moving from left-to-right.
 *
 * The rows for our example matrix:
 *
 * 9, 8, 7
 * 5, 3, 2
 * 6, 6, 7
 *
 * And its columns:
 *
 * 9, 5, 6
 * 8, 3, 6
 * 7, 2, 7
 */
object Matrix {
  def apply(matrixString: String): Matrix = new Matrix(matrixString)
}

case class Matrix(private val matrixString: String) {
  private val matrix: Vector[Vector[Int]] = {
    matrixString.split("\n").foldLeft(Vector[Vector[Int]]()) {
      (matrix: Vector[Vector[Int]], rowString: String) => {
        val cols = rowString.split("\\s")
        if (matrix.length > 0) require(cols.length == matrix(0).length, "all columns must be the same size")
        matrix :+ cols.map(_.toInt).toVector
      }
    }
  }

  def row(index: Int): Vector[Int] =
    if (index < 0 || index >= matrix.length) Vector()
    else matrix(index)

  def column(index: Int): Vector[Int] = {
    if (index < 0 || matrix.length == 0 || index >= matrix(0).length) Vector()
    else matrix.foldLeft(Vector[Int]()) {
      (cols: Vector[Int], rows: Vector[Int]) => cols :+ rows(index)
    }
  }

  // The following are alternatives but not used
  // store columns in a different Vector, with additional memory cost but provides O(1) for the column method
  private val columns = matrix.transpose;
  // Use Option to handle columns of different sizes, but breaks the provided unit tests
  def columnAllowingDifferntSizes(index: Int): Vector[Option[Int]] = {
    matrix.foldLeft(Vector[Option[Int]]()) {
      (cols: Vector[Option[Int]], rows: Vector[Int]) =>
        if (index < 0 || index >= rows.length) cols :+ None
        else cols :+ Some(rows(index))
    }
  }
}
