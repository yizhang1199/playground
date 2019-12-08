package exercises.implicits

/**
 * Implicit Parameters:
 *
 * val/var
 * object
 * def with no parenthesis, e.g. "implicit def compute" works but "implicit def compute()" cannot be used
 *
 * Best Practices:
 * #1 Define the implicit in the companion object if
 * 1.a) there is a single possible value for the implicit parameter
 * 1.b) and you can edit the code for the type
 * #2 Define the good implicit in the companion object and other implicit values elsewhere (preferably either
 * the local scope by developers who use the implicit or other objects that must be explicitly imported)
 * 2.a) there are many possible values for the implicit parameter
 * 2.b) but a single good one for most of the cases
 * 2.c) and you can edit the code for the type
 */
object ImplicitParameters extends App {

  case class Person(name: String, age: Int)

  val Persons = List(
    Person("Bob", 101),
    Person("Amy", 100),
    Person("Lucky", 21))

  // The implicit parameter must appear before it is needed, in this case "(persons.sorted)"
  implicit val DefaultPersonOrdering: Ordering[Person] = Ordering.fromLessThan(_.name < _.name)
  println(Persons.sorted)

  /**
   * Implicit Scope (from highest to lowest).
   *
   * 1. Normal scope = Local scope
   *    Scala will first look for implicit definitions and implicit parameters that can be accessed directly
   *    (without a prefix) at the point the method with the implicit parameter block is called.
   * 2. Imported scope
   * 3. Companion objects of all types involved in the method signature, in this case persons.sorted, which translates
   * to "def sorted[B >: A](implicit ord: Ordering[B]): C" where A is Person, and C is List.  So all types are:
   * List
   * Ordering
   * Person or any supertype of Person
   */
  object AgeOrdering {
    implicit val AgeOrdering: Ordering[Person] = Ordering.fromLessThan(_.age < _.age)
  }

  /**
   * Purchases are sorted in 3 different ways,
   * by total price - most used
   * by unit count - 2nd most used
   * by unit price - 3rd most used -- if this is rarely used, don't even bother defining an ordering, users can
   * always define an ordering in local scope
   */
  case class Purchase(unitCount: Int, unitPrice: Double) {
    def totalPrice: Double = unitPrice * unitCount
  }

  object Purchase {
    implicit val TotalPriceOrdering: Ordering[Purchase] =
      Ordering.fromLessThan((p1, p2) => p1.totalPrice > p2.totalPrice)
  }

  object PurchaseUnitCountOrdering {
    implicit val UnitCountOrdering: Ordering[Purchase] =
      Ordering.fromLessThan((p1, p2) => p1.unitCount > p2.unitCount)
  }

  object PurchaseUnitPriceOrdering {
    implicit val UnitPriceOrdering: Ordering[Purchase] =
      Ordering.fromLessThan((p1, p2) => p1.unitPrice > p2.unitPrice)
  }

  val Purchases: List[Purchase] = List(
    Purchase(3, 2.99),
    Purchase(300, 399.99),
    Purchase(1, 49999.99)
  )

  // By default, Purchase.totalPriceOrdering will be used.  Users can import another ordering if needed to override
  println(Purchases.sorted)
}
