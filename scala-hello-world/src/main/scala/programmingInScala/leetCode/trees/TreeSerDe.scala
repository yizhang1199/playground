package programmingInScala.leetCode.trees

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object TreeSerDe extends App {

  val root = TreeNode(1)
  root.left = Some(TreeNode(2, Some(TreeNode(4)), Some(TreeNode(5))))
  root.right = Some(TreeNode(3, None, Some(TreeNode(6))))

  val serializedStr = serialize(root)
  println(serializedStr)

  val node = deserialize(serializedStr)
  println(s"node=$node")

  private val TreeNodeRegex = """([\d]+),left=(.+),right=(.+)""".r

  case class TreeNode(value: Int, var left: Option[TreeNode] = None, var right: Option[TreeNode] = None)

  class BinaryTree(root: TreeNode) {
    require(root != null)

    def serialize(): String = {
      dfsInorder(root) mkString ","
    }

    private def dfsInorder(node: TreeNode, result: mutable.ListBuffer[String] = mutable.ListBuffer()): Seq[String] = {
      result.addOne(node.value.toString)
      if (node.left.isEmpty) {
        result.addOne("None")
      } else {
        dfsInorder(node.left.get, result)
      }
      if (node.right.isEmpty) {
        result.addOne("None")
      } else {
        dfsInorder(node.right.get, result)
      }

      result.toSeq
    }
  }

  // Encodes a list of strings to a single string.
  def serialize(root: TreeNode): String = {
    new BinaryTree(root).serialize()
  }

  def deserialize(str: String): TreeNode = {
    val tokens = str.split(",").toList
    deserialize(ListBuffer().addAll(tokens)).get
  }

  // 1,2,4,None,None,5,None,None,3,None,6,None,None
  private def deserialize(list: mutable.ListBuffer[String]): Option[TreeNode] = {
    val head = list.remove(0)

    head match {
      case "None" => None
      case _ =>
        val node = Some(TreeNode(head.toInt))
        node.get.left = deserialize(list)
        node.get.right = deserialize(list)
        node
    }
  }
}
