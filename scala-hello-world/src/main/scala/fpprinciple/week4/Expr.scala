package fpprinciple.week4

object test {
  def main(args: Array[String]): Unit = {
    def show(e: Expr): String = e match {
      case Num(n) => n.toString()
      case Sum(left, right) => show(left) + "+" + show(right)
    }

    val e1 = new Num(4)
    println(show(e1))
  }
}

// Pattern matching example
trait Expr {
  private val defaultPriority = 10
  def priority: Int = this match {
    case Num(_) => defaultPriority + 10
    case Sum(_, _) => defaultPriority
    case Prod(_, _) => defaultPriority + 10
    case Var(_) => defaultPriority + 10
    case _ => throw new UnsupportedOperationException("Unknown expr: " + this)
  }
  def eval(): Int
}

case class Num(val value: Int) extends Expr {
  override def eval: Int = value
}

case class Sum(val left: Expr, val right: Expr) extends Expr {
  override def eval: Int = left.eval() + right.eval()
}

case class Prod(val left: Expr, val right: Expr) extends Expr {
  override def eval: Int = left.eval() * right.eval()
}

case class Var(val value: String) extends Expr {
  override def eval: Int = throw new UnsupportedOperationException("Value unkown for " + value)
}