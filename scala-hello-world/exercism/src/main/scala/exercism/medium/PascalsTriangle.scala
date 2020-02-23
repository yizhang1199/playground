package exercism.medium

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
 * https://exercism.io/my/solutions/92acf5336d8240eca9b411e0efc3c4f2
 *
 * Compute Pascal's triangle up to a given number of rows.
 *
 * In Pascal's Triangle each number is computed by adding the numbers to the right and left of the current position in the previous row.
 *
 * 1
 * 1 1
 * 1 2 1
 * 1 3 3 1
 * 1 4 6 4 1
 * # ... etc
 */
object PascalsTriangle {

  //def rows(size: Int): List[List[Int]] = createRows(size).reverse
  def rows(size: Int): List[List[Int]] = getRowsUsingMemoization(size)

  @tailrec
  private def createRows(size: Int, rowsCreatedSoFar: List[List[Int]] = List()): List[List[Int]] = size match {
    case n if n <= 0 => rowsCreatedSoFar
    case row =>
      createRows(row - 1, createNextRow(rowsCreatedSoFar) +: rowsCreatedSoFar) // store rows in reversed order because +: is O(1)
  }

  private def createNextRow(rowsCreatedSoFar: List[List[Int]]): List[Int] = rowsCreatedSoFar match {
    case Nil => List(1)
    case headRow :: _ =>
      (0 +: headRow :+ 0).sliding(2).map(list => list.sum).toList // List(1, 2, 3).sliding(2) will return Iterator(List(1, 2), List(2, 3))
  }

  private def getRowsUsingMemoization(size: Int): List[List[Int]] = {
    lazy val lazyRows: LazyList[List[Int]] = List(1) #:: lazyRows.map(createNextRowLazy)
    lazyRows.take(size).toList
  }

  private def createNextRowLazy(currentRow: List[Int]): List[Int] = {
    val nextRowIndex = currentRow.length
    val result = for {col <- 0 to nextRowIndex} yield col match {
      case 0 | `nextRowIndex` => 1
      case _ => currentRow(col - 1) + currentRow(col)
    }

    result.toList
  }

  // Alternative solutions found in the community
  private def createRowsWith2Folds(rows: Int): List[List[Int]] = {
    (0 until rows).foldRight(List[List[Int]]()) { (i, rows) =>
      (0 until i).foldLeft(ListBuffer(1)) { (row, j) =>
        row :+ (row(j) * (i - j) / (j + 1)) // ListBuffer supports efficient append (and prepend)
      }.toList :: rows
    }
  }
}
