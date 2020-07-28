package programmingInScala.leetCode

import scala.collection.mutable

/**
 * Design and implement a data structure for Least Recently Used (LRU) cache. It should support the following operations: get and put.
 *
 * get(key) - Get the value (will always be positive) of the key if the key exists in the cache, otherwise return -1.
 * put(key, value) - Set or insert the value if the key is not already present. When the cache reached its capacity, it should invalidate the least recently used item before inserting a new item.
 *
 * The cache is initialized with a positive capacity.
 *
 * Follow up:
 * Could you do both operations in O(1) time complexity?
 *
 * Example:
 *
 * LRUCache cache = new LRUCache( 2 /* capacity */ );
 *
 * cache.put(1, 1);
 * cache.put(2, 2);
 * cache.get(1);       // returns 1
 * cache.put(3, 3);    // evicts key 2
 * cache.get(2);       // returns -1 (not found)
 * cache.put(4, 4);    // evicts key 1
 * cache.get(1);       // returns -1 (not found)
 * cache.get(3);       // returns 3
 * cache.get(4);       // returns 4
 */
object LRUCache extends App {

  val cache: Cache = new Cache(2)

  cache.put(1, 1)
  cache.put(2, 2)
  assert(cache.get(1).contains(1))
  cache.put(3, 3)
  assert(cache.get(2).isEmpty)
  cache.put(4, 4)
  assert(cache.get(1).isEmpty)
  println(cache)
  assert(cache.get(3).contains(3))
  println(cache)
  assert(cache.get(4).contains(4))
  println(cache)
  cache.put(4, 5)
  assert(cache.get(4).contains(5))
  println(cache)

  case class LinkedEntry(var value: Int,
                         var previous: Option[LinkedEntry] = None,
                         var next: Option[LinkedEntry] = None)

  case class DLinkedList() {
    private val head: LinkedEntry = LinkedEntry(-1, None, None)
    private val tail: LinkedEntry = LinkedEntry(-2, Some(head), None)
    head.next = Some(tail)

    def removeFirst(): LinkedEntry = {
      require (!head.next.contains(tail))

      val firstEntry = head.next.get
      head.next = firstEntry.next
      firstEntry.next.get.previous = Some(head)

      firstEntry
    }

    def remove(entry: LinkedEntry): Unit = {
      require(entry != head)
      require(entry != tail)

      entry.previous.get.next = entry.next
      entry.next.get.previous = entry.previous
    }

    def append(newEntry: LinkedEntry): LinkedEntry = {
      newEntry.previous = tail.previous
      newEntry.next = tail.previous.get.next

      tail.previous.get.next = Some(newEntry)
      tail.previous = Some(newEntry)

      newEntry
    }

    override def toString(): String = {
      var current = head.next
      var values: Seq[Int] = Seq()

      while (current.isDefined) {
        values = values :+ current.get.value
        current = current.get.next
      }

      values.toString()
    }
  }

  class Cache(val capacity: Int) {
    private val linkedList: DLinkedList = DLinkedList()
    private val map: mutable.Map[Int, LinkedEntry] = mutable.Map()

    def put(key: Int, value: Int): Unit = {
      if (map.contains(key)) {
        val entry = map.get(key)
        entry.get.value = value
        touch(entry)
      } else {
        if (map.size >= capacity) {
          val oldestEntry = linkedList.removeFirst()
          map.remove(oldestEntry.value)
        }

        val newEntry = LinkedEntry(value)
        linkedList.append(newEntry)
        map.put(key, newEntry)
      }
    }

    def get(key: Int): Option[Int] = {
      val entry = map.get(key)

      touch(entry)
      entry.map(_.value)
    }

    private def touch(entry: Option[LinkedEntry]): Unit = {
      entry.foreach { e =>
        linkedList.remove(e)
        linkedList.append(e)
      }
    }

    override def toString: String = {
      s"capacity=$capacity, list=$linkedList"
    }
  }
}
