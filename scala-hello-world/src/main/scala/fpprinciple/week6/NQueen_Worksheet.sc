// N-Queen problem: placing N chess queens on an NxN chessboard so that no two queens threaten each other;
// thus, a solution requires that no two queens share the same row, column, or diagonal.
// nqueen: Given N, find all solutions to place N queens
def nqueen(n: Int): Set[List[(Int, Int)]] = {
  require(n > 0)

  // Given queensPlaced, is it safe to place another queen at position (row, col)
  def isSafe(row: Int, col: Int, queensPlaced: List[(Int, Int)]): Boolean = {
    val isSafe = queensPlaced forall (pair => {
      pair match {
        case (rowPlaced, colPlaced) => colPlaced != col && Math.abs(col - colPlaced) != row - rowPlaced
      }
      //pair._2 != col && (pair._2 - (row - pair._1)) != col && (pair._2 + (row - pair._1)) != col
    })
    //println("isSafe (" + isSafe + "): row=" + row + ", col=" + col + ", queensPlaced=" + queensPlaced)
    isSafe
  }

  def placeQueens(k: Int): Set[List[(Int, Int)]] = {

    if (k == 0) {
      val queensPlacedSet: Set[List[(Int, Int)]] = Set(List())
      println("k=" + k + ": queensPlaced=" + queensPlacedSet)
      queensPlacedSet
    } else {
      val row = k - 1; // row index is 0-based
      val queensPlacedSet: Set[List[(Int, Int)]] = for {
        queensPlaced: List[(Int, Int)] <- placeQueens(k - 1)
        col: Int <- (0 until n) // col index is 0-based
        if isSafe(row, col, queensPlaced)
      } yield (row, col) :: queensPlaced
      println("k=" + k + ": queensPlaced=" + queensPlacedSet)
      queensPlacedSet
    }
  }

  placeQueens(n)
}

def show(queens: List[(Int, Int)]) : String = {
  val length = queens.length
  val lines: List[String] =
    for ((_, col) <- queens.reverse)
      yield (Vector.fill(length)("* ").updated(col, "X ")).mkString
  "\n" + lines.mkString("\n")
}

(nqueen(1) map show) mkString ("\n-----\n")
(nqueen(4) map show) mkString ("\n-----\n")
(nqueen(5) map show) mkString ("\n-----\n")