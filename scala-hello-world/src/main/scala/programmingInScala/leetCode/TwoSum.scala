package programmingInScala.leetCode

/**
 * Given an array of integers, return indices of the two numbers such that they add up to a specific target.
 *
 * You may assume that each input would have exactly one solution, and you may not use the same element twice.
 *
 * Example:
 *
 * Given nums = [2, 7, 11, 15], target = 9,
 *
 * Because nums[0] + nums[1] = 2 + 7 = 9,
 * return [0, 1].
 */
object TwoSum extends App {
  def twoSum(nums: Array[Int], target: Int): Seq[Int] = {
    solve(nums.toIndexedSeq, target)
  }

  def solve(nums: Seq[Int], target: Int): Seq[Int] = {

    val numToIndexMap = (nums zip (0 until nums.size)).toMap

    (0 until nums.length).find {
      index =>
        val companion = target - nums(index)
        numToIndexMap.contains(companion) && numToIndexMap(companion) != index
    } match {
      case Some(index) => Seq(index, numToIndexMap(target - nums(index)))
      case None => Seq()
    }
  }

  val input = Array(230, 863, 916, 585, 981, 404, 316, 785, 88, 12, 70, 435, 384, 778, 887, 755, 740, 337, 86, 92, 325, 422, 815, 650, 920,
    125, 277, 336, 221, 847, 168, 23, 677, 61, 400, 136, 874, 363, 394, 199, 863, 997, 794, 587, 124, 321, 212, 957, 764, 173, 314, 422, 927,
    783, 930, 282, 306, 506, 44, 926, 691, 568, 68, 730, 933, 737, 531, 180, 414, 751, 28, 546, 60, 371, 493, 370, 527, 387, 43, 541, 13, 457,
    328, 227, 652, 365, 430, 803, 59, 858, 538, 427, 583, 368, 375, 173, 809, 896, 370, 789)

  private val result: Seq[Int] = twoSum(input, 542)
  println(result)
  assert(result == Seq(28, 45))
}