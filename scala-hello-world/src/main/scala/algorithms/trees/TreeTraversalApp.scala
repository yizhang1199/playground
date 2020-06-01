package algorithms.trees

object TreeTraversalApp extends App {

  private val binarySearchTree: BinarySearchTree[String] = BinarySearchTree("F")

  binarySearchTree.insert("B", "G", "A", "D", "I", "C", "E", "H")

  println(s"binarySearchTree=$binarySearchTree")

  // in-order: A, B, C, D, E, F, G, H, I
  println(s"inOrderTraversal=${binarySearchTree.inOrderTraversal}")
  assert(Seq("A", "B", "C", "D", "E", "F", "G", "H", "I") == binarySearchTree.inOrderTraversal)
  println(s"inOrderTraversalIterative=${binarySearchTree.inOrderTraversalIterative}")
  assert(binarySearchTree.inOrderTraversal == binarySearchTree.inOrderTraversalIterative)

  // pre-order: F, B, A, D, C, E, G, I, H
  println(s"preOrderTraversal=${binarySearchTree.preOrderTraversal}")
  assert(Seq("F", "B", "A", "D", "C", "E", "G", "I", "H") == binarySearchTree.preOrderTraversal)
  println(s"preOrderTraversalIterative=${binarySearchTree.preOrderTraversalIterative}")
  assert(binarySearchTree.preOrderTraversal == binarySearchTree.preOrderTraversalIterative)

  // post-order: A, C, E, D, B, H, I, G, F
  println(s"postOrderTraversal=${binarySearchTree.postOrderTraversal}")
  assert(Seq("A", "C", "E", "D", "B", "H", "I", "G", "F") == binarySearchTree.postOrderTraversal)
  println(s"postOrderTraversalIterative=${binarySearchTree.postOrderTraversalIterative}")
  assert(binarySearchTree.postOrderTraversal == binarySearchTree.postOrderTraversalIterative)

  // breadth first search: F, B, G, A, D, I, C, E, H
  println(s"breadthFirstSearchIterative=${binarySearchTree.breadthFirstSearchTraversal}")
  assert(Seq("F", "B", "G", "A", "D", "I", "C", "E", "H") == binarySearchTree.breadthFirstSearchIterative)

  println(s"breadthFirstSearchIterative=${binarySearchTree.breadthFirstSearchIterative}")
  println(s"breadthFirstSearchRecursive=${binarySearchTree.breadthFirstSearchRecursive()}")
  assert(Seq("F", "B", "G", "A", "D", "I", "C", "E", "H") == binarySearchTree.breadthFirstSearchRecursive())
}
