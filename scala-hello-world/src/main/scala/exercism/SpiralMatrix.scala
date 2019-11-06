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
    val matrix: Array[Array[Int]] = Array.ofDim[Int](size, size) // All elements have default value 0
    def indexInRange(i: Int): Boolean = i >= 0 && i < size
    def valid(cell: Cell): Boolean =
      indexInRange(cell.row) && indexInRange(cell.col) && matrix(cell.row)(cell.col) == 0

    var cell = Cell(0, -1)
    var direction: Direction = Right
    for ( v <- 1 to size * size ) {
      val nextTry = cell.move(direction) // first try current direction
      if (valid(nextTry)) cell = nextTry
      else {
        direction = direction.change // there is only one other possible direction
        cell = cell.move(direction)
      }
      matrix(cell.row)(cell.col) = v
    }

    (0 until size).foldLeft(List[List[Int]]()) {
      (acc: List[List[Int]], i: Int) => matrix(i).toList +: acc
    }.reverse
  }
}

private case class Cell(row: Int, col: Int) {
  def move(dir: Direction): Cell = dir match {
    case Right => Cell(row, col + 1)
    case Left => Cell(row, col - 1)
    case Up => Cell(row - 1, col)
    case Down => Cell(row + 1, col)
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