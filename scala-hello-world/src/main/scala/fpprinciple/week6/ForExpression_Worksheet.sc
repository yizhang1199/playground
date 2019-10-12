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

def forFun1[T](first: List[T], second: List[T]): List[List[T]] = {
  for {
    t <- second
  } yield first :+ t
}
forFun1(List("a", "b"), List("c", "d"))

def forFun1Equivalent[T](first: List[T], second: List[T]): List[List[T]] = {
  second.map(t => {first :+ t})
}
forFun1Equivalent(List("a", "b"), List("c", "d"))

def forFun2[T](first: List[T], second: List[T]): List[(T, T)] = {
  for {
    t1 <- first
    t2 <- second
  } yield (t1, t2)
}
val callForFun2 = forFun2(List("a", "b"), List("c", "d", "e"))

def forFun2Equivalent[T](first: List[T], second: List[T]): List[(T, T)] = {
  first.flatMap(
    t1 => second.map(
      t2 => (t1, t2))
  )
}
val callForFun2Equivalent = forFun2(List("a", "b"), List("c", "d", "e"))

val teas = List("green", "oolong", "black", "puer", "herb")
val fruits = Set("mango", "peach", "kiwi")
def forFun3(teas: Seq[String], fruits: Set[String]): Seq[(String, String)] = {
  for {
    tea <- teas if tea != "herb"
    fruit <- fruits if fruit.length > 4
  } yield (tea, fruit)
}
val callForFun3 = forFun3(teas, fruits)
// callForFun3: List((green,mango), (green,peach), (oolong,mango), (oolong,peach), (black,mango), (black,peach), (puer,mango), (puer,peach))

def forFun4(teas: Seq[String], fruits: Seq[String]): Seq[(String, String)] = {
  for {
    tea <- teas
    t <- tea if (tea != "herb" && t == 'o')
    fruit <- fruits if fruit == "kiwi"
    f <- fruit if f == 'i'
  } yield (tea, fruit)
}
val callForFun4 = forFun4(teas, Vector("mango", "peach", "kiwi"))
// callForFun4: List((oolong,kiwi), (oolong,kiwi), (oolong,kiwi), (oolong,kiwi), (oolong,kiwi), (oolong,kiwi))

def forFun4Equivalent(teas: Seq[String], fruits: Seq[String]): Seq[(String, String)] = {
  teas.flatMap(tea => {
    tea.withFilter(t => tea != "herb" && t == 'o').flatMap(t => {
      fruits.withFilter(fruit => fruit == "kiwi").flatMap(fruit => {
        fruit.withFilter(f => f == 'i').map(f => {
          (tea, fruit)
        })
      })
    })
  })
}

val anotherWay = forFun4Equivalent(teas, Vector("mango", "peach", "kiwi"))

// for expressions that performs an action but do not yield a value
for (x <- 1 to 3) println(s"iteration $x")

// Mid-stream assignment in a for expression
for {
  x <- 1 to 3 // y cannot be used yet as it hasn't be declared
  y = x^3 // val should not be used, expressions after this can use y
} println(x + "^3 = " + y) // both x and y can be referenced here