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
