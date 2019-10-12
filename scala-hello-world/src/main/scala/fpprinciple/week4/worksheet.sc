import fpprinciple.week4.{Nat, Expr, Num, Sum, List, LinkedList, Successor, Nil, zero, Prod, Var}

val single: List[Int] = new LinkedList[Int](4, Nil)
val list = single.append(5).append(6).append(7);

single.nth(0)
list.nth(2)

val three: Nat = zero.successor.successor.successor
val four: Nat = three.successor
val x: Nat = three + four
val y: Nat = x - three
x - x

val f = (x: Int) => Int
val f1 = (x: Int) => x * x
f(1)
f1(3)

def show(e: Expr): String = {
  def wrap(s: String, includeParentheses: Boolean): String = {
    if (includeParentheses) "(" + s + ")"
    else s
  }

  e match {
    case Num(n) => n.toString()
    case Sum(left, right) => wrap(show(left), e.priority > left.priority) + "+" + wrap(show(right), e.priority > right.priority)
    case Prod(left, right) => wrap(show(left), e.priority > left.priority) + "*" + wrap(show(right), e.priority > right.priority)
    case Var(value) => value
    case null => throw new NullPointerException("expr must not be null")
    case _ => throw new UnsupportedOperationException("Unsupported expr: " + e)
  }
}

val e1 = new Num(2)
println(show(e1))
val e2 = new Sum(Num(3), Num(4))
println(show(e2))
val e3 = new Prod(Num(5), Var("X"))
println(show(e3))
val e4 = new Prod(Num(7), e2)
println(show(e4))
val e5 = new Prod(e4, e2)
println(show(e5))
