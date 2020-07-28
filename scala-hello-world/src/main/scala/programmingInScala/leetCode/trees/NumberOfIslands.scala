package programmingInScala.leetCode.trees

import scala.collection.mutable

/**
 * Given a 2d grid map of '1's (land) and '0's (water), count the number of islands. An island is surrounded by water and is formed by connecting adjacent lands horizontally or vertically.
 * You may assume all four edges of the grid are all surrounded by water.
 *
 * #### Example 1:
 *
 * Input:
 * 11110
 * 11010
 * 11000
 * 00000
 *
 * Output: 1
 *
 * #### Example 2:
 *
 * Input:
 * 11000
 * 11000
 * 00100
 * 00011
 *
 * Output: 3
 */
object NumberOfIslands extends App {

  val inputWithOneIsland = Array(
    Array('1','1','1','1','0'),
    Array('1','1','0','1','0'),
    Array('1','1','0','0','0'),
    Array('0','0','0','0','0')
  )
  assert(numIslands(inputWithOneIsland) == 1)

  val inputWith3Islands = Array(
    Array('1','1','0','0','0'),
    Array('1','1','0','0','0'),
    Array('0','0','1','0','0'),
    Array('0','0','0','1','1')
  )
  assert(numIslands(inputWith3Islands) == 3)

  val inputWithOneIsland2 = Array(
    Array('1','1','1'),
    Array('0','1','0'),
    Array('1','1','1')
  )
  assert(numIslands(inputWithOneIsland2) == 1)

  def numIslands(grid: Array[Array[Char]]): Int = {
    val islands = Islands(grid)

    (islands.yLength, islands.xLength) match {
      case (0, _) => 0
      case (_, 0) => 0
      case _ => islands.count
    }
  }

  private case class Islands(grid: Array[Array[Char]]) {
    case class Node(yIndex: Int, xIndex: Int)
    private[this] val visitedNodes: mutable.Set[Node] = mutable.Set()
    val (yLength, xLength) = getValidatedDimensions
    println(s"yLength=$yLength, xLength=$xLength")

    private def getValidatedDimensions: (Int, Int) = {
      require(grid != null, "grid must not be null")

      var length = -1
      grid.foreach { a =>
        require(a != null, "grid must not contain null Arrays")

        if (length == -1) {
          length = a.length
        } else {
          require(length == a.length, "grid must not contain Arrays of different length")
        }
      }

      (grid.length, length)
    }

    def count: Int = {
      var count = 0

      (0 until yLength).foreach { i =>
        (0 until xLength).foreach { j =>
          val node = Node(i, j)

          if (notVisited(node) && isLand(node)) {
            visitConnectedLands(node)
            count = count + 1
          }
        }
      }

      count
    }

    def notVisited(node: Node): Boolean = {
      !visitedNodes.contains(node)
    }

    def isLand(node: Node): Boolean = {
      grid(node.yIndex)(node.xIndex) == '1'
    }

    def visitConnectedLands(node: Node): Unit = {
      visitedNodes.addOne(node)

      moveRight(node).foreach(visitConnectedLands)
      moveLeft(node).foreach(visitConnectedLands)
      moveDown(node).foreach(visitConnectedLands)
      moveUp(node).foreach(visitConnectedLands)
    }

    def moveRight(node: Node): Option[Node] = {
      unvisitedLandNodeAt(node.yIndex, node.xIndex + 1)
    }

    def moveLeft(node: Node): Option[Node] = {
      unvisitedLandNodeAt(node.yIndex, node.xIndex - 1)
    }

    def moveDown(node: Node): Option[Node] = {
      unvisitedLandNodeAt(node.yIndex + 1, node.xIndex)
    }

    def moveUp(node: Node): Option[Node] = {
      unvisitedLandNodeAt(node.yIndex - 1, node.xIndex)
    }

    private def unvisitedLandNodeAt(yIndex: Int, xIndex: Int): Option[Node] = {
      val node = Node(yIndex, xIndex)

      if (isValidNode(node) && notVisited(node) && isLand(node)) {
        Some(node)
      } else {
        None
      }
    }

    private def isValidNode(node: Node): Boolean = {
      node.xIndex >= 0 && node.xIndex < xLength && node.yIndex >= 0 && node.yIndex < yLength
    }
  }
}
