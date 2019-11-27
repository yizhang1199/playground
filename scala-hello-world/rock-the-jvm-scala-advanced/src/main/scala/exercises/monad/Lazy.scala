package exercises.monad

/**
 * A Monad that encapsulates "lazy evaulation once and only once".  This class satisfies the following monadic laws:
 *
 * Associativity: (m flatMap f) flatMap g ≡ m flatMap (x => f(x) flatMap g)
 * Left identity/unit: unit(x) flatMap f ≡ f(x)
 * Right identity/unit: m flatMap unit ≡ m
 *
 * @param value An expensive expression that will be evaluated once only when evaluate is called for the first time
 * @tparam T type of value
 */
class Lazy[+T](value: => T) {
  // use lazy val to evaluate value lazily and only once
  lazy val compute: T = value

  // 1. use by-name parameter for f's input; otherwise, value will be eagerly evaluated when flatMap is called
  // 2. pass compute instead of value to f; otherwise, value will be evaluated each time f is called
  def flatMap[U](f: (=> T) => Lazy[U]): Lazy[U] = f(compute)
}

object Lazy {
  def apply[T](value: => T): Lazy[T] = new Lazy(value)

  def main(args: Array[String]): Unit = {
    // instantiating a Lazy instance should not cause the message to be printed
    val lazyInstance: Lazy[Int] = Lazy {
      println("###Creating lazyInstance: 1234")
      1234
    }
    println(s"Created lazyInstance=$lazyInstance.  We should not see 'Creating lazyInstance' printed out yet")
    val lazyInstance2: Lazy[Int] = Lazy {
      println("###Creating lazyInstance2: 5678")
      5678
    }
    println(s"Created lazyInstance2=$lazyInstance2.  We should not see 'Creating lazyInstance' printed out yet")

    println("\nCall lazyInstance.evaluate")
    println(s"lazyInstance.evaluate=${lazyInstance.compute}")

    println("\nCall lazyInstance2.flatMap")
    val lazyInstance3 = lazyInstance2.flatMap(x => Lazy {
      println("###Creating lazyInstance3 via lazyInstance2.flatMap")
      x + 1
    })

    println("\nCall lazyInstance2.flatMap again")
    lazyInstance2.flatMap(x => Lazy {
      println("###Creating lazyInstance3 via lazyInstance2.flatMap")
      x - 1
    })

    println("\nCall lazyInstance3.evaluate")
    println(s"lazyInstance3.evaluate=${lazyInstance3.compute}")

    println("\nCall lazyInstance2.evaluate")
    println(s"lazyInstance2.evaluate=${lazyInstance2.compute}")
  }
}
