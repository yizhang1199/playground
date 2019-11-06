import scala.collection.immutable.HashMap

val capitalOfCountry: Map[String, String] = Map("US" -> "Washington", "China" -> "Beijing")

def printCapital(country: String)(function1: String => String): Unit = {
  println(country + ": " + function1(country))
}

// Maps are also functions, e.g. a Map instance can be used as a function value/instance
printCapital("China")(capitalOfCountry) // China: Beijing
//printCapital("Blah")(capitalOfCountry) // throws NoSuchElementException

// Map.get will return Option[ValueType] instead of the value directly
def printCapital2(country: String): Unit = capitalOfCountry.get(country) match {
  case None => println(country + ": capital not found")
  case Some(capital) => println(country + ": " + capital)
}

printCapital2("China")
printCapital2("Blah")

val mapWithListValues = Map("key1" -> List("a", "b"), "key2" -> List("c", "d", "e"))
val foreachTest = mapWithListValues.foreach {
  p: (String, List[String]) => println(p._1 +: p._2)
}

val testOptionFilter = capitalOfCountry.get("US").filter(p => p.contains("x"))
mapWithListValues.get("key1").filter(p => p.length > 2)
mapWithListValues.get("key2").filter(p => p.length > 2)

val testOptionMap: Option[String] = mapWithListValues.get("key1") map (value => value mkString "+")

/**
 * map, flatMap, and collect
 */
val mapWithSeqValues: Map[String, Seq[String]] =
  Map("teas" -> Seq("green", "oolong"),
    "fruits" -> Seq("mango", "kiwi", "peach"),
    "nuts" -> Seq("almond", "mac"))
// maps every (String, Seq[String]) to Seq[(String, Int)]
// returns: Map(3555939 -> teas+green+oolong, -1265922337 -> fruits+mango+kiwi+peach, 3393158 -> nuts+almond+mac)
val flatMapTest: Map[Int, String] = mapWithSeqValues
  .flatMap[Int, String] {
    p: (String, Seq[String]) => Seq((p._1.hashCode, (p._1 +: p._2).mkString("+")))
  }
// maps every (String, Seq[String]) to Seq[String]
// returns: List(teas, green, oolong, fruits, mango, kiwi, peach, nuts, almond, mac)
val flatMapTest2: Iterable[String] = mapWithSeqValues.flatMap[String] {
  p: (String, Seq[String]) => p._1 +: p._2
}
// returns: List(teas, green, oolong)
val flatMapTestWithFilter = mapWithSeqValues
  .flatMap[String] {
    case (key, values) if key == "teas" => key +: values
    case _ => Seq() // required because flatMap takes a function, not a partial function
  }
// maps every (String, Seq[String]) to (String, Int)
// returns: Map(teas -> 2, fruits -> 3, nuts -> 2)
val mapTest: Map[String, Int] = mapWithSeqValues.map[String, Int] {
  p: (String, Seq[String]) => (p._1, p._2.length)
}
// maps every (String, Seq[String]) to Seq[String]
// returns: List(List(teas, green, oolong), List(fruits, mango, kiwi, peach), List(nuts, almond, mac))
val mapTest2: Iterable[Seq[String]] = mapWithSeqValues.map[Seq[String]] {
  p: (String, Seq[String]) => p._1 +: p._2
}
// mapTestWithFilter is equivalent to collectTest
// returns: List(List(teas, green, oolong))
val mapTestWithFilter = mapWithSeqValues
  .filter(p => p._1 == "teas")
  .map[Seq[String]] { case (key, values) => key +: values }
// returns: List(List(teas, green, oolong), List(), List())
// notice the last List()
val mapTestWithFilter2 = mapWithSeqValues
  .map[Seq[String]] {
    case (key, values) if key == "teas" => key +: values
    case _ => Seq() // required because map takes a function, not a partial function
  }
// collect, unlike map, takes a partial function.
// collect will only call pf.apply(element) if pf.isDefinedAt(element) is true
// this means collect can replace chaining filter with map
// returns: List(List(teas, green, oolong))
val collectTest = mapWithSeqValues.collect[Seq[String]] {
  case (key: String, values: Seq[String]) if key == "teas" => key +: values
}
// collectTest2 is equivalent to collectTest
// returns: List(List(teas, green, oolong))
val pf = new PartialFunction[(String, Seq[String]), Seq[String]] {
  def apply(x: (String, Seq[String])): Seq[String] = x._1 +: x._2

  def isDefinedAt(x: (String, Seq[String])) = x._1 == "teas"
}
val collectTest2 = mapWithSeqValues.collect[Seq[String]](pf)

/**
 * groupBy, partition, span, splitAt: split sequences into subsets in Scala
 */
val groupByTest = mapWithSeqValues.groupBy {
  pair: (String, Seq[String]) => pair._1 == "teas"
}
val groupByTestExpected = HashMap(
  false -> Map("fruits" -> List("mango", "kiwi", "peach"), "nuts" -> List("almond", "mac")),
  true -> Map("teas" -> List("green", "oolong")))
assert(groupByTest == groupByTestExpected)
val partitionTest = mapWithSeqValues.partition(_._1 == "teas")
val partitionTestExpected = (
  Map("teas" -> List("green", "oolong")), // Left
  Map("fruits" -> List("mango", "kiwi", "peach"), "nuts" -> List("almond", "mac")) // Right
)
val (leftPartition, rightPartition) = partitionTest
assert(partitionTest == partitionTestExpected)
val spanTest = mapWithSeqValues.span(_._1 == "teas")
val spanTestExpected = partitionTestExpected
assert(spanTest == spanTestExpected)
val (leftSpan, rightSpan) = spanTest
val splitAtTest = mapWithSeqValues.splitAt(2)
val splitAtTestExpected = (
  Map("teas" -> List("green", "oolong"), "fruits" -> List("mango", "kiwi", "peach")), // Left
  Map("nuts" -> List("almond", "mac")) // Right
)
assert(splitAtTest == splitAtTestExpected)

// difference between partition and span, because span "Returns the longest prefix of the
// list whose elements all satisfy the given predicate, and the rest of the list."
// span is equivalent to (takeWhile(p), dropWhile(p)) provided p has no side effects
val partitionTest2 = mapWithSeqValues.partition(_._1 == "fruits")
val partitionTest2Expected = (
  Map("fruits" -> List("mango", "kiwi", "peach")), // Left
  Map("teas" -> List("green", "oolong"), "nuts" -> List("almond", "mac")) // Right
)
assert(partitionTest2 == partitionTest2Expected)
val spanTest2 = mapWithSeqValues.span(_._1 == "fruits")
val spanTest2Expected = (
  Map(), // Left
  Map("teas" -> List("green", "oolong"), "fruits" -> List("mango", "kiwi", "peach"), "nuts" -> List("almond", "mac")) // Right
)
assert(spanTest2 == spanTest2Expected)

// grouped Partitions elements in fixed size collections (the last one may not have enough) and returns an Iterator
val groupedTest = mapWithSeqValues.grouped(1)
//groupedTest.next() // Map("teas" -> List("green", "oolong"))
val groupedTestExpectedList = List(
  Map("teas" -> List("green", "oolong")),
  Map("fruits" -> List("mango", "kiwi", "peach")),
  Map("nuts" -> List("almond", "mac")))
assert(groupedTestExpectedList == groupedTest.toList)

val groupedTest2 = mapWithSeqValues.grouped(2)
//groupedTest2.next() // Map("teas" -> List("green", "oolong"), "fruits" -> List("mango", "kiwi", "peach"))
val groupedTest2ExpectedList = List(
  Map("teas" -> List("green", "oolong"), "fruits" -> List("mango", "kiwi", "peach")),
  Map("nuts" -> List("almond", "mac"))) // last Map element only has 1 Key->Value
assert(groupedTest2ExpectedList == groupedTest2.toList)

mapWithSeqValues.unapply("teas") // Some(List(green, oolong))
mapWithSeqValues.get("teas") // Some(List(green, oolong))
mapWithSeqValues.apply("teas") // List(green, oolong)
// also see sample implementations for map and flatMap in Monad_Worksheet.sc