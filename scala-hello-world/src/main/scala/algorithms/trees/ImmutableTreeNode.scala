package algorithms.trees

import java.util.NoSuchElementException


trait ImmutableTreeNode[+T] {
  def get: Option[T]

  def left: ImmutableTreeNode[T]

  def right: ImmutableTreeNode[T]

//  def insert(newValue: T)(implicit ordering: Ordering[T]): Unit
}

case class SimpleTreeNode[+T](value: T, left: ImmutableTreeNode[T], right: ImmutableTreeNode[T])
  extends ImmutableTreeNode[T] {
  require(value != null)
  require(left != null)
  require(right != null)

  override def get: Option[T] = Some(value)
}

object SimpleTreeNode {
  def apply[T](value: T): SimpleTreeNode[T] = new SimpleTreeNode[T](value, NilTreeNode, NilTreeNode)
}

object NilTreeNode extends ImmutableTreeNode[Nothing] {
  override def get: Option[Nothing] = None

  override def left: ImmutableTreeNode[Nothing] = throw new NoSuchElementException("NilTreeNode.left")

  override def right: ImmutableTreeNode[Nothing] = throw new NoSuchElementException("NilTreeNode.right")
}