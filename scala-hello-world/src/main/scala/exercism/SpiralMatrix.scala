package exercism

/**
 * https://exercism.io/my/solutions/fa59f57f2a3c4602bc37b804b877cd89
 *
 * Given the size, return a square matrix of numbers in spiral order.
 *
 * The matrix should be filled with natural numbers, starting from 1 in the top-left corner, increasing in an inward, clockwise spiral order, like these examples:
 * Spiral matrix of size 3
 *
 * 1 2 3
 * 8 9 4
 * 7 6 5
 *
 * Spiral matrix of size 4
 *
 * 1  2  3 4
 * 12 13 14 5
 * 11 16 15 6
 * 10  9  8 7
 */
object SpiralMatrix {
  def spiralMatrix(size: Int): List[List[Int]] = {
    if (size <= 0) List()
    else InternalMatrix(size).spiralMatrix
  }
}

private case class InternalMatrix(size: Int) {
  require(size > 0)
  private val matrix: Array[Array[Int]] = Array.ofDim[Int](size, size) // All elements have default value 0
  val spiralMatrix: List[List[Int]] = {
    var cell = Cell()
    for ( v <- 1 to size * size ) {
      cell = cell.next
      matrix(cell.rowIndex)(cell.colIndex) = v
    }

    (0 until size).foldLeft(List[List[Int]]()) {
      (acc: List[List[Int]], i: Int) => matrix(i).toList +: acc
    }.reverse
  }

  private def valid(cell: Cell): Boolean = {
    cell.rowIndex >= 0 && cell.rowIndex < size &&
      cell.colIndex >= 0 && cell.colIndex < size &&
      matrix(cell.rowIndex)(cell.colIndex) == 0
  }

  private case class Cell(rowIndex: Int = -1, colIndex: Int = -1, direction: Direction = Right) {
    def next: Cell = (rowIndex, colIndex) match {
      case (-1, -1) => Cell(0, 0, direction)
      case _ =>
        val nextCell = move(this.direction) // start with the direction that created this Cell
        if (valid(nextCell)) nextCell
        else move(this.direction.change) // there is only one other direction to try
    }

    private def move(dir: Direction): Cell = dir match {
      case Right => Cell(rowIndex, colIndex + 1, dir)
      case Left => Cell(rowIndex, colIndex - 1, dir)
      case Up => Cell(rowIndex - 1, colIndex, dir)
      case Down => Cell(rowIndex + 1, colIndex, dir)
    }
  }
}

private sealed trait Direction {
  def change: Direction
}

private case object Right extends Direction {
  override val change: Direction = Down
}

private case object Left extends Direction {
  override val change: Direction = Up
}

private case object Up extends Direction {
  override val change: Direction = Right
}

private case object Down extends Direction {
  override val change: Direction = Left
}