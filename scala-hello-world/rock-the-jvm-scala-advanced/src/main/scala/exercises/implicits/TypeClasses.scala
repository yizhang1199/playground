package exercises.implicits

/**
 * Type classes can be used to enrich existing types (aka classes) with a new interface/trait without changing
 * existing code. Type class solves adhoc polymorphism at compile-time, which ensures type safety.
 *
 * 1. Type class, e.g. MyEqual[T]
 * 2. Type class instances, e.g. NameEquality, FullUserEquality.  These are quite often marked as implicit
 * 3. Two usage options:
 * 3.1 Define companion object for the type class to hide type class instances, e.g. MyEqual[User].areEqual(amy1, amy2)
 * 3.2 (preferred): Define implicit conversion classes that enrich/convert other classes to use the type
 *     class.  This options allows us to write very expressive code with type safety, e.g. bob1 tsEqual bob2
 */
object TypeClasses extends App {

  case class User(name: String, age: Int, email: String)

  // Part 1: Type class
  trait MyEqual[T] {
    def areEqual(t1: T, t2: T): Boolean

    def somethingElse: String = "something else"
  }

  // Part 2: Type class instance1, often declared implicit and lives in its own class file to avoid ambiguity
  // Can't mark NameEquality as implicit here due to ambiguity with FullUserEquality
  object NameEquality extends MyEqual[User] {
    override def areEqual(user1: User, user2: User): Boolean = user1.name == user2.name
  }

  // Part 2: Type class instance2
  implicit object FullUserEquality extends MyEqual[User] {
    override def areEqual(user1: User, user2: User): Boolean = user1.name == user2.name && user1.email == user2.email
  }

  /**
   * Part 3: Usage (with 2 options)
   */
  // 3.1: Define companion object for Equal[T]
  object MyEqual {
    // With this, compiler will translate MyEqual[T] to MyEqual.apply(instance) where instance is implicitly
    // picked by the compiler, in this case FullUserEquality.
    // And since instance must implement all abstract methods in the type class, this approach essentially
    // gives us access to the entire type class.
    def apply[T](implicit instance: MyEqual[T]): MyEqual[T] = instance

    // With the apply method, this is not necessary as the equal method must be implemented on all instances
    // of the type class
    // def equal[T](t1: T, t2: T)(implicit equality: Equal[T]): Boolean = equality.equal(t1, t2)
  }

  val amy1 = User("Amy", 32, "amy@gmail.com")
  val amy2 = User("Amy", 23, "amy2@gmail.com")

  // MyEqual[User] will be desugared to MyEqual.apply(FullEquality) because FullEquality is the
  // implicit Equal[User] type in local scope
  val fullEquality = MyEqual[User].areEqual(amy1, amy2)
  val fullEqualityEquivalent = MyEqual.apply.areEqual(amy1, amy2) // explicitly call apply
  MyEqual(FullUserEquality).areEqual(amy1, amy2) // apply can be called the "usual" way
  assert(fullEquality == fullEqualityEquivalent)
  println(s"fullEquality: amy1 == amy2: $fullEquality")

  // Use NameEquality
  val nameEquality = MyEqual[User](NameEquality).areEqual(amy1, amy2) // explicitly pass in an instance of Equal[User]
  val nameEqualityEquivalent = MyEqual.apply(NameEquality).areEqual(amy1, amy2) // explicitly call apply
  MyEqual(NameEquality).areEqual(amy1, amy2) // another way
  assert(nameEquality == nameEqualityEquivalent)
  println(s"nameEquality: amy1 == amy2: $nameEquality")

  println(s"Equal[User].somethingElse: ${MyEqual[User].somethingElse}")
  //println(s"Equal[User].somethingElse: ${Equal.somethingElse}") // Won't compile

  /**
   * 3.2 (preferred): Define implicit conversion classes and use context bounds to enrich/convert existing classes
   */
  implicit class TypeSafeEqual[T](val value: T) extends AnyVal {
    def tsEqual(anotherValue: T)(implicit myEqual: MyEqual[T]): Boolean = { // use context bound
      myEqual.areEqual(value, anotherValue)
    }

    // TODO this fails with "type mismatch" but why?
//    def tsEqual[T: MyEqual](anotherValue: T): Boolean = { // use context bound syntax sugar, preferred
//      val myEqual = implicitly[MyEqual[T]]
//      myEqual.areEqual(value, anotherValue)
//    }
  }

  val bob1 = User("Bob", 111, "bob1@gmail.com")
  val bob2 = User("Bob", 222, "bob2@gmail.com")

  bob1 tsEqual bob2 // compiler translates this to something like "new TypeSafeEqual(bob1).tsEqual(bob2)(FullUserEquality)"
  TypeSafeEqual(bob1).tsEqual(bob2)
  bob1.tsEqual(bob2)(NameEquality)
  println(s"bob1==bob2: ${bob1 tsEqual bob2}; implicit used: ${implicitly[MyEqual[User]]}") // false
  println(s"bob1==bob2: ${bob1.tsEqual(bob2)(NameEquality)}") // true
}
