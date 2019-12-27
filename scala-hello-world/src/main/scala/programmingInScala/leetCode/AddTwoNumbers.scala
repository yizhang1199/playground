package programmingInScala.leetCode

import scala.annotation.tailrec

/**
 * You are given two non-empty linked lists representing two non-negative integers. The digits are stored in reverse
 * order and each of their nodes contain a single digit. Add the two numbers and return it as a linked list.
 *
 * You may assume the two numbers do not contain any leading zero, except the number 0 itself.
 *
 * Example:
 *
 * Input: (2 -> 4 -> 3) + (5 -> 6 -> 4)
 * Output: 7 -> 0 -> 8
 * Explanation: 342 + 465 = 807.
 */
object AddTwoNumbers extends App {

  def solve(list1: List[Int], list2: List[Int]): List[Int] = {
    require(list1 != null && list2 != null)

    val sum = add(list1, list2)
    sum.reverse
  }

  @tailrec
  private def add(list1: List[Int], list2: List[Int], carryover: Int = 0, acc: List[Int] = List()): List[Int] = {
    (list1, list2) match {
      case (x :: _, y :: _) =>
        val (sum, c) = addNumbers(x, y, carryover)
        add(list1.tail, list2.tail, c, sum :: acc)
      case (x :: _, List()) =>
        val (sum, c) = addNumbers(x, carryover)
        add(list1.tail, list2, c, sum :: acc)
      case (List(), y :: _) =>
        val (sum, c) = addNumbers(y, carryover)
        add(list1, list2.tail, c, sum :: acc)
      case (Seq(), Seq()) =>
        if (carryover > 0) carryover :: acc
        else acc
    }
  }

  private def addNumbers(num: Int*): (Int, Int) = {
    val sum = num.sum
    (sum % 10, sum / 10)
  }

  assert(List(7, 0, 8) == solve(List(2, 4, 3), List(5, 6, 4)))
  assert(List(0, 1, 6) == solve(List(2, 4, 5), List(8, 6)))
}
