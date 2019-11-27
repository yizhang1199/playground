package exercises.implicits

object TypeClasses extends App {

  case class User(name: String, age: Int, email: String)

  trait Equal[T] { // Type class
    def equal(t1: T, t2: T): Boolean

    def somethingElse: String = "something else"
  }

  object Equal { // companion object for Equal[T]
    // With this, compiler will translate Equal[T] to Equal.apply(instance) which simply returns the instance.
    // And since instance is an instance of the type class, it must implement all abstract methods in the type class.
    // This essentially gives us access to the entire type class interface, implemented by "instance".
    // For type classes, Equal[T] is similar to Some("..")
    // Equal[T] has access to all methods defined in the type class Equal[T]
    // whereas Equal only has access to methods in the Equal singleton object
    def apply[T](implicit instance: Equal[T]): Equal[T] = instance

    // With the apply method, this is not necessary as the equal method must be implemented on all instances
    // of the type class
    //def equal[T](t1: T, t2: T)(implicit equality: Equal[T]): Boolean = equality.equal(t1, t2)

    def blah: String = "blah"
  }

  object NameEquality extends Equal[User] { // Type class instance
    override def equal(user1: User, user2: User): Boolean = user1.name == user2.name
  }

  implicit object FullEquality extends Equal[User] { // Type class instance
    override def equal(user1: User, user2: User): Boolean = user1.name == user2.name && user1.email == user2.email
  }

  val amy1 = User("Amy", 32, "amy@gmail.com")
  val amy2 = User("Amy", 23, "amy2@gmail.com")

  // Use FullEquality
  val fullEquality = Equal[User].equal(amy1, amy2) // FullEquality is marked as implicit in local scope
  val fullEqualityEquivalent = Equal.apply.equal(amy1, amy2) // explicitly call apply
  Equal(FullEquality).equal(amy1, amy2) // apply can be called the "usual" way
  assert(fullEquality == fullEqualityEquivalent)
  println(s"fullEquality: amy1 == amy2: $fullEquality")

  // Use NameEquality
  val nameEquality = Equal[User](NameEquality).equal(amy1, amy2) // explicitly pass in an instance of Equal[User]
  val nameEqualityEquivalent = Equal.apply(NameEquality).equal(amy1, amy2) // explicitly call apply
  Equal(NameEquality).equal(amy1, amy2) // another way
  assert(nameEquality == nameEqualityEquivalent)
  println(s"nameEquality: amy1 == amy2: $nameEquality")

  println(s"Equal[User].somethingElse: ${Equal[User].somethingElse}")
  //println(s"Equal[User].somethingElse: ${Equal.somethingElse}") // Won't compile
}
