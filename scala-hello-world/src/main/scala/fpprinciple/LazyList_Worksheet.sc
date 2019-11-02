
val fibs: LazyList[BigInt] =
  BigInt(0) #:: BigInt(1) #:: fibs.zip(fibs.tail).map { n => n._1 + n._2 }
fibs take 5 foreach println

val range: LazyList[Long] = 0L #:: range.map(_ + 1)
range take 3 foreach (println(_))

object MySingleton {
  // As long as MySingleton is referenced, it will hold on to ALL realized elements in range1 (memoization), potential
  // memory concern if a huge # of elements have been realized/computed, especially over the lifetime of an application
  //val range1: LazyList[Long] = 1L #:: range1.map(_ + 1)
  val range1: LazyList[Long] = 1L #:: range1.map{ e =>
    val element = e + 1
    print(s"calculated $element, ") // add side effect to see when the function is called
    element
  }

  // Since lazyRange is scoped to range2, memoization is also limited within range2; in other words, a
  // new lazyRange will be created each time range2() is called and all elements must be re-calculated
  def range2: Unit = {
    // without the "lazy", we get runtime error: forward reference extends over definition of value range
    lazy val lazyRange: LazyList[Long] = 2L #:: lazyRange.map { e =>
      val element = e + 1
      print(s"calculated $element, ")
      element
    }
    println("; range2-1: " + (lazyRange take 6 mkString " "))
    // elements in lazyRange will be reused since we are still in the scope of range2
    println("; range2-2: " + (lazyRange take 6 mkString " "))
  }

  // each time range3() is called, a new LazyList will be created, which means
  // all elements will be re-calculated again, and we lose the benefit of memoization.
  def range3(initVal: Long = 3L): LazyList[Long] = initVal #:: range3 {
    val element = initVal + 1
    print(s"calculated $element, ")
    element
  }
}

println("range1-1: " + (MySingleton.range1 take 6 mkString " "))
println("range1-2: " + (MySingleton.range1 take 6 mkString " "))
MySingleton.range2
MySingleton.range2
println("range3: " + (MySingleton.range3() take 6 mkString " "))
println("range3: " + (MySingleton.range3() take 6 mkString " "))
