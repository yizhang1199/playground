package fpprinciple

object LambdaTest {
  def main(args: Array[String]): Unit = {
    val test = List(1, 2 ,3)
    val double = test.map(x => x * 2)

    println(double)
  }
}
