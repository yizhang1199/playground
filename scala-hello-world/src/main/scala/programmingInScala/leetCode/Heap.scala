package programmingInScala.leetCode

import scala.collection.mutable.PriorityQueue

object Heap {
  def findKthLargest(nums: Array[Int], k: Int): Int = {
    require(nums != null)
    require(k > 0 && k <= nums.length)

    val heap = PriorityQueue(nums(0))

    nums.foreach { num =>
      heap.enqueue(num)
    }

    var answer: Int = 0

    (1 to k).foreach { _ =>
      answer = heap.dequeue()
    }

    answer
  }
}
