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
  def spiralMatrix(size: Int): Seq[Seq[Int]] = {
    if (size <= 0) List()
    else generate(size)
  }

  private def generate(size: Int): List[List[Int]] = {
    val matrix: Array[Array[Int]] = Array.fill[Int](size, size)(0)

    def indexInRange(i: Int): Boolean = i >= 0 && i < size

    def isLegal(cell: Cell): Boolean =
      indexInRange(cell.row) && indexInRange(cell.col) && matrix(cell.row)(cell.col) == 0

    var cell = Cell()
    for ( v <- 1 to size * size ) {
      cell = cell.next(isLegal).get
      matrix(cell.row)(cell.col) = v
    }

    matrix.foldLeft(List[List[Int]]()) {
      (acc: List[List[Int]], row: Array[Int]) => row.toList +: acc
    }.reverse
  }

  // some community solutions https://exercism.io/tracks/scala/exercises/spiral-matrix/solutions/04684321c9134a0f892017f295a6ed92
}

/**
 * A cell knows its own coordinates and the direction that got the cell here.  It knows how to move to the next cell
 * based on the existing coordinates and direction.  It is; however, not aware of the matrix in which it resides
 *
 * @param row row index
 * @param col col index
 * @param direction direction in which the cell moved
 */
private case class Cell(row: Int = 0, col: Int = -1, direction: Direction = Right) {
  /**
   * Finds the next legal cell reachable from this cell.
   *
   * @param isLegal a function that tests if a cell is legal in the broader context.
   * @return
   */
  def next(isLegal: Cell => Boolean): Option[Cell] = {
    val possibleDirections = List(direction, direction.change)
    possibleDirections.map(move).find(isLegal) // find the first legal move
  }

  private def move(dir: Direction): Cell = dir match {
    case Right => Cell(row, col + 1, dir)
    case Left => Cell(row, col - 1, dir)
    case Up => Cell(row - 1, col, dir)
    case Down => Cell(row + 1, col, dir)
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