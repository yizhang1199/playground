package algorithms.trees

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.collection.mutable

case class BinarySearchTree[T](private val value: T) {

  private val root: TreeNode[T] = TreeNode(value)

  def insert(values: T*)(implicit ordering: Ordering[T]): Unit = {
    require(value != null && values.nonEmpty)
    values.foreach(insert)
  }

  def insert(newValue: T)(implicit ordering: Ordering[T]): Unit = {
    require(value != null)
    insert(newValue, root)
  }

  /**
   * Breadth first search: visit every node on a level before going to a lower level
   */
  def breadthFirstSearchTraversal: Seq[T] = {
    breadthFirstSearchRecursive()
  }

  private def breadthFirstSearchRecursive(node: TreeNode[T] = root, result: Queue[T] = Queue()): Queue[T] = {
    val parentResult = result :+ node.value
    val leftResult = breadthFirstSearchRecursive(node.left, parentResult)
    breadthFirstSearchRecursive(node.right, leftResult)
  }

  def breadthFirstSearchIterative: Queue[T] = {
    def collectChildren(node: TreeNode[T]): Queue[TreeNode[T]] = {
      var children: Queue[TreeNode[T]] = Queue()
      if (node.left != null) {
        children = children :+ node.left
      }

      if (node.right != null) {
        children = children :+ node.right
      }
      children
    }

    var result: Queue[T] = Queue(root.value)
    var children: Queue[TreeNode[T]] = collectChildren(root)
    while (children.nonEmpty) {
      children = children.foldLeft(Queue[TreeNode[T]]()) {
        (acc, c) =>
          result = result :+ c.value
          acc.concat(collectChildren(c))
      }
    }

    result
  }

  /**
   * pre-order traversal (depth first search): for each node (starting at root): parent, left, right
   */
  def preOrderTraversal: Seq[T] = {
    preOrderTraversalRecursive()
  }

  private def preOrderTraversalRecursive(node: TreeNode[T] = root, result: Queue[T] = Queue()): Queue[T] = {
    if (node == null) {
      result
    } else {
      val parentValue = node.value
      val newResult = preOrderTraversalRecursive(node.left, result :+ parentValue)
      preOrderTraversalRecursive(node.right, newResult)
    }
  }

  // Traversal order: parent, left, right
  def preOrderTraversalIterative: Queue[T] = {
    var node = root
    var result: Queue[T] = Queue()
    val stack: mutable.Stack[TreeNode[T]] = mutable.Stack(root)

    while (stack.nonEmpty) {
      node = stack.pop()
      result = result :+ node.value
      if (node.right != null) {
        stack.push(node.right)
      }
      if (node.left != null) {
        stack.push(node.left)
      }
    }

    result
  }

  /**
   * post-order traversal (depth first search): for each node (starting at root): left, right, parent
   */
  def postOrderTraversal: Seq[T] = {
    postOrderTraversalRecursive()
  }

  private def postOrderTraversalRecursive(node: TreeNode[T] = root, result: Queue[T] = Queue()): Queue[T] = {
    if (node == null) {
      result
    } else {
      val leftResult = postOrderTraversalRecursive(node.left, result)
      val rightResult = postOrderTraversalRecursive(node.right, leftResult)
      rightResult :+ node.value
    }
  }

  // Traversal order: left, right, parent
  def postOrderTraversalIterative: Queue[T] = {
    var result: Queue[T] = Queue()
    val stack: mutable.Stack[TreeNode[T]] = mutable.Stack()
    var node = root
    var lastNodeValueVisited: T = root.value
    while (stack.nonEmpty || node != null) {
      if (node != null) {
        stack.push(node)
        node = node.left
      } else {
        node = stack.pop()
        if (node.right != null && node.right.value != lastNodeValueVisited) {
          stack.push(node)
          node = node.right
        } else {
          lastNodeValueVisited = node.value
          result = result :+ lastNodeValueVisited
          node = null
        }
      }
    }

    result
  }

  /**
   * In order traversal (depth first search): for each node (starting at root): left, parent, right
   */
  def inOrderTraversal: Seq[T] = {
    inOrderTraversalRecursive()
  }

  //@tailrec
  private def inOrderTraversalRecursive(node: TreeNode[T] = root, result: Queue[T] = Queue()): Queue[T] = {
    if (node == null) {
      result
    } else {
      val leftResult = inOrderTraversalRecursive(node.left, result)
      val parentResult = leftResult :+ node.value
      inOrderTraversalRecursive(node.right, parentResult)
    }
  }

  // Traversal order: left, parent, right
  def inOrderTraversalIterative: Queue[T] = {
    var result: Queue[T] = Queue()
    val stack: mutable.Stack[TreeNode[T]] = mutable.Stack()
    var node: TreeNode[T] = root

    while (node != null || stack.nonEmpty) {
      if (node != null) {
        stack.push(node)
        node = node.left
      } else {
        node = stack.pop()
        result = result :+ node.value
        node = node.right
      }
    }

    result
  }

  override def toString: String = s"BinarySearchTree: $root"

  @tailrec
  private def insert(newValue: T, node: TreeNode[T])(implicit ordering: Ordering[T]): Unit = {
    //print(s"newValue=$newValue, node=${node.value}, ")
    ordering.compare(newValue, node.value) match {
      case lessThan() =>
        if (node.left == null) {
          node.setLeft(TreeNode(newValue))
        } else {
          insert(newValue, node.left)
        }
      case equalTo() =>
      case greaterThan() =>
        if (node.right == null) {
          node.setRight(TreeNode(newValue))
        } else {
          insert(newValue, node.right)
        }
    }
  }

  private object lessThan {
    def unapply(result: Int): Boolean = result < 0
  }

  private object equalTo {
    def unapply(result: Int): Boolean = result == 0
  }

  private object greaterThan {
    def unapply(result: Int): Boolean = result > 0
  }

}
