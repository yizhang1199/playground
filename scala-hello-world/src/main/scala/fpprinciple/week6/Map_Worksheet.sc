val capitalOfCountry: Map[String, String] = Map("US" -> "Washington", "China" -> "Beijing")

def printCapital(country: String)(function1: String => String): Unit = {
  println(country + ": " + function1(country))
}

// Maps are also functions, e.g. a Map instance can be used as a function value/instance
printCapital("China")(capitalOfCountry)
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

// terms: key->exponent value->coefficient
class Poly(origTerms: Map[Int, Double]) {
  val terms: Map[Int, Double] = origTerms withDefaultValue 0.0

  def this(bindings: (Int, Double)*) = this(bindings.toMap)

  def add(other: Poly): Poly = {
    val combinedTerms: Map[Int, Double] = terms ++ (other.terms map adjust)
    new Poly(combinedTerms)
  }

  private def adjust(term: (Int, Double)): (Int, Double) = {
    val (exp, coefficient) = term
    exp -> (coefficient + terms(exp))
    //(exp, coefficient + terms(exp))
  }

  def +(other: Poly): Poly = {
    val combinedTerms: Map[Int, Double] = (other.terms foldLeft terms) (addTerm)
    new Poly(combinedTerms)
  }

  private def addTerm(terms: Map[Int, Double], term: (Int, Double)): Map[Int, Double] = {
    val (exponent, coefficient) = term
    //terms.updated(exponent, coefficient + terms(exponent))
    terms + (exponent -> (coefficient + terms(exponent)))
  }

  override def toString: String = {
    //println("terms.toList: " + terms.toList)
    val value = for {
      // Map.toList will return a list of pairs: List[Pair(K, V)]
      // (exponent, coefficient) <- terms will return K,V pairs in unsorted order
      (exponent, coefficient) <- terms.toList.sorted.reverse
      if coefficient != 0
    } yield {
      if (exponent == 0) String.valueOf(coefficient)
      else if (exponent == 1) coefficient + "X"
      else coefficient + "X^" + exponent
    }

    //println(value)
    value mkString " + "
  }
}

val poly1 = new Poly(1 -> 2.0, 0 -> 8, 3 -> 2, 8 -> 3)
val poly2 = new Poly(1 -> 0.5, 0 -> 2, 2 -> 2)
val poly3 = poly1 + poly2

val mapData : Map[String, Seq[String]] =
  Map("teas" -> List("green", "oolong"), "fruits" -> List("mango", "kiwi", "peach"))
// map does not support flatMap so need to convert seq first
val flatMapTest : Seq[String] = mapData.toSeq.flatMap( p => {
  p match { case (key, value) => value.prepended(key) }
})

// TODO fails with "error: missing parameter type" why?
//val flatMapTest2 : Set[String] = mapData.toSet.flatMap(p => {
//  p match { case (key, value) => (key::value).toSet }
//})

val setData = Set("green", "oolong", "black", "puer")
val flaMapTest3 = setData.flatMap(_.toCharArray) // same as (p => p.toCharArray)

// See sample implementations for map and flatMap in Monad_Worksheet.sc