package algorithms.trees

class TreeNode[T](private var nodeValue: T, private var leftNode: TreeNode[T], private var rightNode: TreeNode[T]) {
  setValue(nodeValue)

  def value: T = this.nodeValue

  def left: TreeNode[T] = this.leftNode

  def right: TreeNode[T] = this.rightNode

  // Scala generates a weird setter method value_$eq(t: T) and will auto-translate value_=(t: T) to value_$eq(t: T)
  def setValue(newValue: T): Unit = {
    require(value != null)
    this.nodeValue_=(newValue)
  }

  def setLeft(node: TreeNode[T]): Unit = {
    this.leftNode_=(node)
  }

  def setRight(node: TreeNode[T]): Unit = {
    this.rightNode_=(node)
  }

  override def toString: String = {
    (leftNode, rightNode) match {
      case (null, null) => s"<$nodeValue>"
      case (null, r) => s"[null, $nodeValue, $r]"
      case (l, null) => s"[$l, $nodeValue, null]"
      case _ => s"[$leftNode, $nodeValue, $rightNode]"
    }
  }
}

object TreeNode {
  def apply[T](value: T, left: TreeNode[T], right: TreeNode[T]): TreeNode[T] = new TreeNode[T](value, left, right)

  def apply[T](value: T): TreeNode[T] = new TreeNode[T](value, null, null)
}