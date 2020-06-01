package programmingInScala.leetCode.trees

import scala.collection.mutable
import scala.collection.mutable.Queue

object BinaryTree extends App {

  private val myTree = Node(value = 1,
    left = Some(Node(2, left = Some(Node(4)))),
    right = Some(Node(3, right = Some(Node(5)))))

  println("myTree=" + myTree)
  println("breadthFirstTraversal=" + breadthFirstTraversal(myTree))

  println("preOrderTraversal=" + preOrderTraversal(myTree))
  println("preOrderTraversalIterative=" + preOrderTraversalIterative(myTree))

  println("inOrderTraversal=" + inOrderTraversal(myTree))
  println("inOrderTraversalIterative=" + inOrderTraversalIterative(myTree))

  println("postOrderTraversal=" + postOrderTraversal(myTree))
  println("postOrderTraversalIterative=" + postOrderTraversalIterative(myTree))

  println("zigZagLevelOrderTraversal=" + zigZagLevelOrderTraversal(myTree))

  private def zigZagLevelOrderTraversal(root: Node): Seq[Seq[Int]] = {
    val result: mutable.ListBuffer[Seq[Int]] = mutable.ListBuffer()
    var queue: mutable.Queue[Node] = mutable.Queue[Node](root)
    var leftToRight = true

    while (queue.nonEmpty) {
      val innerResult = mutable.ListBuffer[Int]()

      if (leftToRight) {
        queue.foldLeft(innerResult)((acc, node) => acc.append(node.value))
      } else {
        queue.foldRight(innerResult)((node, acc) => acc.append(node.value))
      }
      leftToRight = !leftToRight
      result.append(innerResult.toSeq)

      val newNodesToProcess = mutable.Queue[Node]()
      while(queue.nonEmpty) {
        val node = queue.dequeue()
        node.left.foreach(newNodesToProcess.enqueue)
        node.right.foreach(newNodesToProcess.enqueue)
      }

      queue = newNodesToProcess
    }

    result.toSeq
  }

  private def breadthFirstTraversal(root: Node): Seq[Int] = {
    require(root != null)
    val result: mutable.Queue[Int] = mutable.Queue()
    val queue: mutable.Queue[Node] = mutable.Queue(root)

    while(queue.nonEmpty) {
      val node = queue.dequeue
      result.enqueue(node.value)

      node.left.foreach(queue.enqueue)
      node.right.foreach(queue.enqueue)
    }

    result.toSeq
  }

  // Pre-order: parent, left, right
  private def preOrderTraversal(node: Node, result: Queue[Int] = Queue()): Seq[Int] = {
    result.enqueue(node.value)
    node.left.foreach(n => preOrderTraversal(n, result))
    node.right.foreach(n => preOrderTraversal(n, result))

    result.toSeq
  }

  private def preOrderTraversalIterative(node: Node): Seq[Int] = { // parent, left, right
    val result: mutable.Queue[Int] = mutable.Queue()
    val stack: mutable.Stack[Node] = mutable.Stack(node)

    while (stack.nonEmpty) {
      val current = stack.pop()

      result.enqueue(current.value)
      current.right.foreach(stack.push)
      current.left.foreach(stack.push)
    }

    result.toSeq
  }

  // In-order: left, parent, right
  private def inOrderTraversal(node: Node, result: mutable.Queue[Int] = Queue()): Seq[Int] = {
    node.left.foreach(n => inOrderTraversal(n, result))
    result.enqueue(node.value)
    node.right.foreach(n => inOrderTraversal(n, result))

    result.toSeq
  }

  private def inOrderTraversalIterative(node: Node): Seq[Int] = {
    val result: mutable.Queue[Int] = mutable.Queue()
    val visited: mutable.Set[Node] = mutable.Set()
    val stack: mutable.Stack[Node] = mutable.Stack(node)

    while (stack.nonEmpty) {
      val current = stack.pop()

      if (!visited.contains(current) && hasAtLeastOneChild(current)) {
        visited.add(current)
        current.right.foreach(stack.push)
        stack.push(current)
        current.left.foreach(stack.push)
      } else {
        result.enqueue(current.value)
      }
    }

    result.toSeq
  }

  // Post-order: left, right, parent
  private def postOrderTraversal(node: Node, result: Queue[Int] = Queue()): Seq[Int] = {
    node.left.foreach(n => postOrderTraversal(n, result))
    node.right.foreach(n => postOrderTraversal(n, result))
    result.enqueue(node.value)

    result.toSeq
  }

  private def postOrderTraversalIterative(node: Node): Seq[Int] = {
    val result: Queue[Int] = Queue()
    val stack: mutable.Stack[Node] = mutable.Stack(node)
    val visited: mutable.Set[Node] = mutable.Set()

    while (stack.nonEmpty) {
      val current = stack.pop()

      if (!visited.contains(current) && hasAtLeastOneChild(current)) {
        stack.push(current)
        current.right.foreach(stack.push)
        current.left.foreach(stack.push)
        visited.add(current)
      } else {
        result.enqueue(current.value)
      }
    }

    result.toSeq
  }

  private def hasAtLeastOneChild(node: Node): Boolean = {
    node.left.isDefined || node.right.isDefined
  }

  case class Node(value: Int, left: Option[Node] = None, right: Option[Node] = None)
}

