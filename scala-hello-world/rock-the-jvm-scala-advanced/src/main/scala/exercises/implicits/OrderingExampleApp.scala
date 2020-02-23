package exercises.implicits

object OrderingExampleApp extends App {

  val names = List("John", "Sue", "Olivia", "Kiana")

  // The default StringOrdering (provided in the Ordering companion object) will be used
  println(names.sorted) // List(John, Kiana, Olivia, Sue)

  import OrderingExample.ReverseStringOrdering
  println(names.sorted) // List(Sue, Olivia, Kiana, John)

//  import OrderingExample.StringLengthOrdering
//  println(names.sorted) // List(Sue, John, Kiana, Olivia)
}
