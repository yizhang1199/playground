package exercism

object Matrix {
  def apply(matrixString: String): Matrix = new Matrix(matrixString)
}

case class Matrix(private val matrixString: String) {
  private val matrix: Vector[Vector[Int]] = {
    matrixString.split("\n").foldLeft(Vector[Vector[Int]]()) {
      (matrix: Vector[Vector[Int]], rowString: String) => {
        val cols = rowString.split("\\s")
        if (matrix.length > 0) require(cols.length == matrix(0).length, "all columns must be the same size")
        matrix :+ cols.map(s => Integer.parseInt(s)).toVector
      }
    }
  }

  private val columns = matrix.transpose;
  def row(index: Int): Vector[Int] =
    if (index < 0 || index >= matrix.length) Vector()
    else matrix(index)

  def column(index: Int): Vector[Int] = {
    if (index < 0 || matrix.length == 0 || index >= matrix(0).length) Vector()
    else matrix.foldLeft(Vector[Int]()) {
      (cols: Vector[Int], rows: Vector[Int]) => cols :+ rows(index)
    }
  }

  // Not used: alternative to use Option to handle columns of different sizes, but breaks the provided unit tests
  def columnAllowingDifferntSizes(index: Int): Vector[Option[Int]] = {
    matrix.foldLeft(Vector[Option[Int]]()) {
      (cols: Vector[Option[Int]], rows: Vector[Int]) =>
        if (index < 0 || index >= rows.length) cols :+ None
        else cols :+ Some(rows(index))
    }
  }
}
