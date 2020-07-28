package programmingInScala.leetCode.linkedLists

import scala.annotation.tailrec

/**
 * Given a linked list, reverse the nodes of a linked list k at a time and return its modified list.
 *
 * k is a positive integer and is less than or equal to the length of the linked list. If the number of nodes is not
 * a multiple of k then left-out nodes in the end should remain as it is.
 *
 * Example:
 *
 * Given this linked list: 1->2->3->4->5
 *
 * For k = 2, you should return: 2->1->4->3->5
 *
 * For k = 3, you should return: 3->2->1->4->5
 *
 * Note:
 *
 * Only constant extra memory is allowed.
 * You may not alter the values in the list's nodes, only nodes itself may be changed.
 */
object ReverseNodesInKGroups extends App {

  case class ListNode(value: Int, var next: Option[ListNode] = None) // TODO don't use var

  private def list1 = buildList(1, 2, 3, 4, 5, 6)

  println(s"list=$list1")
  println(s"reversed list=${reverseLinkedList(list1)}")
  println(s"4th node=${findKthNode(list1, 4)}")
  println(s"reverse first 4 nodes=${reverseFirstKNodes(list1, 4)}")
  println(s"reverse 2 nodes repeatedly=${reverseKNodesRepeatedly(list1, 2)}")

  def buildList(values: Int*): Option[ListNode] = {
    val dummyHead: ListNode = ListNode(0)

    values.foldLeft(dummyHead) { (current, value) =>
      current.next = Some(ListNode(value))
      current.next.get
    }

    dummyHead.next
  }

  def reverseKNodesRepeatedly(root: Option[ListNode], k: Int): Option[ListNode] = {
    (root, k) match {
      case (_, x) if x <= 1 => root
      case (None, _) => root
      case _ =>
        val dummyHead = ListNode(0, root)
        var currentDummy = dummyHead
        var head = currentDummy.next
        var kthNode = findKthNode(head, k)
        var stopNode = kthNode.flatMap(_.next)

        while (kthNode.isDefined) {
          reverseUtil(currentDummy, stopNode)

          currentDummy = head.get
          head = currentDummy.next
          kthNode = findKthNode(head, k)
          stopNode = kthNode.flatMap(_.next)
        }

        dummyHead.next
    }
  }

  @tailrec
  private def findKthNode(start: Option[ListNode], k: Int, kthNode: Option[ListNode] = None): Option[ListNode] = {
    (start, k) match {
      case (None, _) => None
      case (_, k) if k <= 0 => kthNode
      case _ => findKthNode(start.flatMap(_.next), k - 1, start)
    }
  }

  private def reverseFirstKNodes(start: Option[ListNode], k: Int): Option[ListNode] = {
    val kthNode = findKthNode(start, k)

    if (kthNode.isDefined) {
      val stopNode = kthNode.flatMap(_.next)
      reverseLinkedList(start, stopNode, stopNode)
    } else {
      start
    }
  }

  private def reverseUtil(dummyHead: ListNode, stopNode: Option[ListNode] = None): Unit = {
    val reversed = reverseLinkedList(dummyHead.next, stopNode, stopNode)
    dummyHead.next = reversed
  }

  @tailrec
  private def reverseLinkedList(head: Option[ListNode],
                                stopNode: Option[ListNode] = None,
                                reversedHead: Option[ListNode] = None): Option[ListNode] = {
    if (head.isDefined && head != stopNode) {
      val nextHead = head.flatMap(_.next)

      head.get.next = reversedHead
      val newReversedHead = head

      reverseLinkedList(nextHead, stopNode, newReversedHead)
    } else {
      reversedHead
    }
  }

  @tailrec
  private def reverse(head: ListNode, reversed: Option[ListNode] = None): ListNode = {
    require(head != null)

    head match {
      case ListNode(_, None) =>
        head.next = reversed
        head
      case ListNode(_, Some(next)) =>
        head.next = reversed
        reverse(next, Some(head))
    }
  }
}
