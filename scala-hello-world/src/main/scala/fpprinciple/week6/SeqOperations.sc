
val listOfPairs: IndexedSeq[(Int, Character)] = {
  (1 to 4) flatMap (x => {
    ('a' to 'c') map (y => (x, y))
  })
}

val listOfListOfPairs: IndexedSeq[IndexedSeq[(Int, Char)]] = {
  (1 to 4) map (x => {
    ('a' to 'c') map (y => (x, y))
  })
}

def isPrime(n: Int): Boolean = {
  require(n > 0)
  // TODO this works even for n <= 2, but how?
  !((2 until n) exists (p => (n % p) == 0))
}

(1 to 10) map (x => (x, isPrime(x)))

// for n, find (i, j) pairs such that 1 <= j < i < n and i+j is a prime number
def findPrimePairs(n: Int) : Seq[(Int, Int)] = {
  (2 until n) flatMap (i =>
    (1 until i) map (j => (i, j))
    ) filter (p => isPrime(p._1 + p._2))
}

findPrimePairs(10)

def findPrimePairs2(n: Int) : Seq[(Int, Int)] = {
  for {
    i <- (2 until n)
    j <- (1 until i)
    if isPrime(i + j)
  } yield (i, j)
}

findPrimePairs2(8)

def scalarProduct(l1: Seq[Double], l2: Seq[Double]): Double = {
  val temp: Seq[Double] = for {
    (x, y) <- (l1 zip l2)
  } yield (x * y)
  //temp.foldLeft(0.0)((startValue, element) => startValue + element)
  (temp foldLeft 0.0)(_ + _)
}

scalarProduct(List(1, 2, 3), List(3, 2, 1, 4))
