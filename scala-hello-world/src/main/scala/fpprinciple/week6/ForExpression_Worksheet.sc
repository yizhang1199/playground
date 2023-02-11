
// Introduction to for-expression: https://www.youtube.com/watch?v=Jf6Us5bSOJ4&list=PLTeAcbOFvqaGtSMysJzzcYhrZyzpKyvKN&index=3&t=0s
// N-Queen problem:

def concat[T](first: List[T], second: List[T]): List[T] = {
  (first foldRight second) ((t, list) => t :: list)
}

concat(List("a", "b"), List("c", "d"))

def concat2[T](first: List[T], second: List[T]): List[T] = {
  (second foldLeft first) ((list, t) => list :+ t)
}

concat2(List("a", "b"), List("c", "d"))

val zipTest = List("a", "b") zip List("c", "d", "e")

/**
 * for (p0 <- expr0) yield expr
 *    is translated to
 * expr0.map(p0 => expr)
 *
 * for {p0 <- expr0
 *      p1 <- expr1
 *      p2 <- expr2
 *      ...
 *      pX <- exprX}
 * yield expr
 *
 *    is translated recursively using flatMap for expr1 through exprX-1 and map for exprX
 *
 * expr0.flatMap {p0 =>
 *   for {p1 <- expr1
 *        p2 <- expr2
 *        ...
 *        pX <- exprX}
 *   yield expr
 * }
 */
def forFun1[T](first: List[T], second: List[T]): List[List[T]] = {
  for {
    t <- second
  } yield first :+ t
}
forFun1(List("a", "b"), List("c", "d")) // List(List(a, b, c), List(a, b, d))

def forFun1Equivalent[T](first: List[T], second: List[T]): List[List[T]] = {
  second.map(t => {
    first :+ t
  })
}
forFun1Equivalent(List("a", "b"), List("c", "d"))

def forFun2[T](first: List[T], second: List[T]): List[(T, T)] = {
  for {
    t1 <- first
    t2 <- second
  } yield (t1, t2)
}
// callForFun2: List[(String, String)] = List((a,c), (a,d), (a,e), (b,c), (b,d), (b,e))
val callForFun2 = forFun2(List("a", "b"), List("c", "d", "e"))

def forFun2Equivalent[T](first: List[T], second: List[T]): List[(T, T)] = {
  first.flatMap(
    t1 => second.map(
      t2 => (t1, t2))
  )
}
// callForFun2Equivalent: List[(String, String)] = List((a,c), (a,d), (a,e), (b,c), (b,d), (b,e))
val callForFun2Equivalent = forFun2(List("a", "b"), List("c", "d", "e"))

val teas = List("green", "oolong", "black", "puer", "herb")
val fruits = Set("mango", "peach", "kiwi")
def forFun3(teas: Seq[String], fruits: Set[String]): Seq[(String, String)] = {
  for {
    tea <- teas if tea != "herb"
    fruit <- fruits if fruit.length > 4 // excludes kiwi
  } yield (tea, fruit)
}
val callForFun3 = forFun3(teas, fruits)
// callForFun3: List((green,mango), (green,peach), (oolong,mango), (oolong,peach), (black,mango), (black,peach), (puer,mango), (puer,peach))
def forFun3Equivalent(teas: Seq[String], fruits: Set[String]): Seq[(String, String)] = {
  teas
    .withFilter(tea => tea != "herb")
    .flatMap(t =>
      fruits
        .withFilter(fruit => fruit.length > 4)
        .map(f => (t, f)))
}
val callForFun3Equivalent = forFun3Equivalent(teas, fruits)
assert(callForFun3 == callForFun3Equivalent)

def forFun4(teas: Seq[String], fruits: Seq[String]): Seq[(String, String)] = {
  for {
    tea <- teas
    teaChar <- tea if tea != "herb" && teaChar == 'o' // for oolong, this will loop 3 times
    fruit <- fruits if fruit == "kiwi"
    fruitChar <- fruit if fruitChar == 'i' // for kiwi, this will loop 2 times
  } yield (tea, fruit)
}
val callForFun4 = forFun4(teas, fruits.toList)
// callForFun4: List((oolong,kiwi), (oolong,kiwi), (oolong,kiwi), (oolong,kiwi), (oolong,kiwi), (oolong,kiwi))
def forFun4Equivalent(teas: Seq[String], fruits: Seq[String]): Seq[(String, String)] = {
  teas.flatMap(tea => {
    tea
      .withFilter(teaChar => tea != "herb" && teaChar == 'o')
      .flatMap(teaChar => {
        fruits
          .withFilter(fruit => fruit == "kiwi")
          .flatMap(fruit => {
            fruit
              .withFilter(fruitChar => fruitChar == 'i')
              .map(fruitChar => (tea, fruit))
          })
      })
  })
}
val callForFun4Equivalent = forFun4Equivalent(teas, fruits.toList)
assert(callForFun4 == callForFun4Equivalent)

// for expressions that performs an action but do not yield a value
// Mid-stream assignment in a for expression
for {
  x <- 1 to 3 // y cannot be used yet as it hasn't be declared
  y = x ^ 3 // val should not be used, expressions after this can use y
} println(s"$x^3 = $y") // both x and y can be referenced here

/**
 *
 */
def generatePermutations(list: List[Int]): List[List[Int]] = list match {
  case List() | _ :: Nil => List(list)
  case _ =>
    for {listItem <- list
         subList = list.filter(_ != listItem) // define a local val
         subPermList <- generatePermutations(subList)} yield {
      listItem +: subPermList
    }
}
println("permutations(1, 2, 3)=" + generatePermutations(List(1, 2, 3)))

/**
 * Use Applicatives with Disjunctions
 */
case class MyError(error: String)
val error1 = MyError("error1")
val result1: Either[MyError, Int] = Right(100)
val result2: Either[MyError, Int] = Left(error1)
val result3: Either[MyError, Int] = Right(200)

val combinedResult1 = for {
  r1 <- result1
  r2 <- result3
} yield r1 + r2 // Right(100)

val combinedResult2 = for {
  r1 <- result1
  r2 <- result2
} yield r1 + r2 // Left(MyError("error1"))