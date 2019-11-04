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

val testOptionFilter = capitalOfCountry.get("US").filter(p => p.contains("x"))
mapWithListValues.get("key1").filter(p => p.length > 2)
mapWithListValues.get("key2").filter(p => p.length > 2)

val testOptionMap: Option[String] = mapWithListValues.get("key1") map (value => value mkString "+")

/**
 * map, flatMap, and collect
 */
val mapData: Map[String, Seq[String]] =
  Map("teas" -> List("green", "oolong"), "fruits" -> List("mango", "kiwi", "peach"))
// maps every (String, Seq[String]) to Seq[(String, Int)]
// returns: Map(3555939 -> teas+green+oolong,
//              -1265922337 -> fruits+mango+kiwi+peach)
val flatMapTest: Map[Int, String] = mapData
  .flatMap[Int, String] {
    p: (String, Seq[String]) => Seq((p._1.hashCode, (p._1 +: p._2).mkString("+")))
  }
// maps every (String, Seq[String]) to Seq[String]
// returns: List(teas, green, oolong, fruits, mango, kiwi, peach)
val flatMapTest2: Iterable[String] = mapData.flatMap[String] {
  p: (String, Seq[String]) => p._1 +: p._2
}
// returns: List(teas, green, oolong)
val flatMapTestWithFilter = mapData
  .flatMap[String] {
    case (key, values) if key == "teas" => key +: values
    case _ => Seq() // required because flatMap takes a function, not a partial function
  }
// maps every (String, Seq[String]) to (String, Int)
// returns: Map(teas -> 2, fruits -> 3)
val mapTest: Map[String, Int] = mapData.map[String, Int] {
  p: (String, Seq[String]) => (p._1, p._2.length)
}
// maps every (String, Seq[String]) to Seq[String]
// returns: List(List(teas, green, oolong), List(fruits, mango, kiwi, peach))
val mapTest2: Iterable[Seq[String]] = mapData.map[Seq[String]] {
  p: (String, Seq[String]) => p._1 +: p._2
}
// mapTestWithFilter is equivalent to collectTest
// returns: List(List(teas, green, oolong))
val mapTestWithFilter = mapData
  .filter(p => p._1 == "teas")
  .map[Seq[String]] { case (key, values) => key +: values }
// returns: List(List(teas, green, oolong), List())
// notice the last List()
val mapTestWithFilter2 = mapData
  .map[Seq[String]] {
    case (key, values) if key == "teas" => key +: values
    case _ => Seq() // required because map takes a function, not a partial function
  }
// collect, unlike map, takes a partial function
// this means collect can replace chaining filter with map
// returns: List(List(teas, green, oolong))
val collectTest = mapData.collect[Seq[String]] {
  case (key: String, values: Seq[String]) if key == "teas" => key +: values
}
// collectTest2 is equivalent to collectTest
// returns: List(List(teas, green, oolong))
val pf = new PartialFunction[(String, Seq[String]), Seq[String]] {
  def apply(x: (String, Seq[String])): Seq[String] = x._1 +: x._2
  def isDefinedAt(x: (String, Seq[String])) = x._1 == "teas"
}
val collectTest2 = mapData.collect[Seq[String]](pf)

val setData = Set("green", "oolong", "black", "puer")
// returns: HashSet(e, n, u, a, b, p, c, r, k, o, g, l)
val flaMapTest3 = setData.flatMap(_.toCharArray) // same as (p => p.toCharArray)

// see sample implementations for map and flatMap in Monad_Worksheet.sc