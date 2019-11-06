

// O( N * N)
def insertionSort(in: List[Int]): List[Int] = {
  def insert(cell: Int, sortedList: List[Int]): List[Int] = sortedList match {
    case List() => List(cell)
    case head :: tail => {
      if (cell <= head) cell :: sortedList // prepends cell to list, which forms a new list
      else head :: insert(cell, tail)
    }
  }

  in match {
    case List() => in
    case head :: tail => insert(head, insertionSort(tail))
  }
}

var l = List(5, 1, 3, 2, 4)
insertionSort(l)

// O(N)
def last[T](list: List[T]): T = list match {
  case List() => throw new NoSuchElementException("Nil.last")
  case List(x) => x // matching is sequential, this will match a list of single element first
  case _ :: tail => last(tail)
}

last(l)

// O( N * N)
def reverse[T](list: List[T]): List[T] = list match {
  case List() => list
  case List(x) => list
  case head :: tail => reverse(tail) ++ List(head)
}

reverse(l)

def removeAt[T](list: List[T], i: Int): List[T] = list match {
  case List() => throw new IndexOutOfBoundsException("i=" + i + ", length=0")
  case head :: tail =>
    if (i < 0 || i >= list.length) throw new IndexOutOfBoundsException("i=" + i + ", length=" + list.length)
    else if (i == 0) tail
    else head :: removeAt(tail, i - 1)
}

removeAt(l, 1)

def mergeSort[T](list: List[T])(lt: (T, T) => Boolean): List[T] = {
  def merge(sorted1: List[T], sorted2: List[T]): List[T] = {
    (sorted1, sorted2) match {
      case (Nil, _) => sorted2
      case (_, Nil) => sorted1
      case (head1 :: tail1, head2 :: tail2) =>
        if (lt(head1, head2)) head1 :: merge(tail1, sorted2)
        else head2 :: merge(sorted1, tail2)
    }
  }

  list match {
    case Nil => list
    case _ :: Nil => list
    case _ => {
      val n = list.length / 2
      val (first, second) = list splitAt n
      merge(mergeSort(first)(lt), mergeSort(second)(lt))
    }
  }
}

mergeSort(Nil)(null)
mergeSort(l)((x, y) => x < y)
mergeSort(List("jackfruit", "mangosteen", "durian", "start fruit", "lychee"))((x, y) => x.compareTo(y) < 0)

def mergeSort2[T](list: List[T])(implicit order: Ordering[T]): List[T] = {
  def merge(sorted1: List[T], sorted2: List[T]): List[T] = {
    (sorted1, sorted2) match {
      case (Nil, _) => sorted2
      case (_, Nil) => sorted1
      case (head1 :: tail1, head2 :: tail2) =>
        if (order.lt(head1, head2)) head1 :: merge(tail1, sorted2)
        else head2 :: merge(sorted1, tail2)
    }
  }

  list match {
    case Nil => list
    case _ :: Nil => list
    case _ => {
      val n = list.length / 2
      val (first, second) = list splitAt n
      merge(mergeSort2(first), mergeSort2(second))
    }
  }
}

mergeSort2(List("jackfruit", "mangosteen", "durian", "peach", "mango"))

//@tailrec
def squareList(list: List[Int]): List[Int] = list match {
  case Nil => list
  case head :: tail => head * head :: squareList(tail)
}

def squareList2(list: List[Int]): List[Int] = {
  list map (x => x * x)
}

squareList(List(3, 2, 8, 7))
squareList2(List(3, 2, 8, 7))

def pack[T](list: List[T]): List[List[T]] = list match {
  case null => Nil
  case Nil => Nil
  case head :: _ => {
    val (first, second) = list span (x => x == head)
    first :: pack(second)
  }
}

pack(null)
pack(Nil)
pack(List("apple", "apple", "mango", "apple", "mango", "mango"))

def encode[T](list: List[T]): List[(T, Int)] =  {
  pack(list) map (l => (l.head, l.length))
}

encode(null)
encode(Nil)
encode(List("apple", "apple", "mango", "apple", "mango", "mango"))

// compose
val list1 = List("Hello", "Kitty", "And", "World")
def findIndex(s: String): Int = s.hashCode % list1.length
// compose will take the input function and return a new function
// composed with this.apply, e.g. list1.apply(findIndex(arguement))
val afterCompose: String => String = list1.compose(findIndex(_))
val input = "A cow jumped over the moon"
// The following 3 method calls are exactly the same
afterCompose(input)
list1.apply(findIndex(input))
list1(findIndex(input))

Array.tabulate(2, 2)((x, y) => {
  println(s"x=$x, y=$y")
  x + y
})

val intArray = Array.fill[Int](3, 3)(0)
intArray.transpose
