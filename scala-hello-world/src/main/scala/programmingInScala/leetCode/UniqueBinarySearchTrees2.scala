package programmingInScala.leetCode

import scala.annotation.tailrec

/**
 * https://leetcode.com/explore/learn/card/recursion-i/253/conclusion/2384/
 *
 * Given an integer n, generate all structurally unique BST's (binary search trees) that store values 1 ... n.
 *
 * Example:
 *
 * Input: 3
 * Output:
 * [
 * [1,null,3,2],
 * [3,2,null,1],
 * [3,1,null,null,2],
 * [2,1,3],
 * [1,null,2,null,3]
 * ]
 * Explanation:
 * The above output corresponds to the 5 unique BST's shown below:
 *
 * 1         3     3      2      1
 * \       /     /      / \      \
 * 3     2     1      1   3      2
 * /     /       \                 \
 * 2     1         2                 3
 */
object UniqueBinarySearchTrees2 extends App {
  println("generateTrees(3):\n" + (generateTrees(3) mkString "\n"))

  println("permutations:" + permutations(List(1, 2, 3)))

  def generateTrees(n: Int): List[TreeNode] = n match {
    case 0 => List()
    case _ => generateTrees((1 to n).toList)
  }

  private def generateTrees(list: List[Int]): List[TreeNode] = {
    list match {
      case List() => List()
      case head +: Nil => List(new TreeNode(head))
      case _ =>
        for {
          value <- list
          subList: List[Int] = list diff List(value)
          subTree <- generateTrees(subList)
        } yield {
          insertValue(subTree, value)
          subTree
        }
    }
  }

  @tailrec
  private def insertValue(node: TreeNode, value: Int): Unit = value match {
    case x: Int if x == node.value =>
    case x: Int if x < node.value =>
      if (node.left == null) node.left = new TreeNode(value)
      else insertValue(node.left, value)
    case _ =>
      if (node.right == null) node.right = new TreeNode(value)
      else insertValue(node.right, value)
  }

  private def isEqual(tree1: TreeNode, tree2: TreeNode): Boolean = (tree1, tree2) match {
    case (null, null) => true
    case (null, _) => false
    case (_, null) => false
    case (t1, t2) => t1.value == t2.value && isEqual(t1.left, t1.left) && isEqual(t1.right, t2.right)
  }

  private def permutations(list: List[Int]): List[List[Int]] = list match {
    case List() | _ :: Nil => List(list)
    case _ =>
      for {item <- list
           subList = list.filter(_ != item)
           subPermList <- permutations(subList)} yield {
        item +: subPermList
      }
  }
}

class TreeNode(var _value: Int) { // class provided by leetcode, can't change
  var value: Int = _value
  var left: TreeNode = null
  var right: TreeNode = null

  override def toString(): String = (left, right) match {
    case (null, null) => s"[$value]"
    case (null, _) => s"[$value.$right]"
    case (_, null) => s"[$left.$value]"
    case _ => s"[$left.$value.$right]"
  }
}