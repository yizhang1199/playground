
println("""Special chars, except 3 consecutive " are escaped.\nI won't start on a newline""")
println("""|Welcome to Ultamix 3000.
           |Type "HELP" for help.""".stripMargin)

// Symbol literals, e.g. '<identifier> has been deprecated in scala 2.13
//the literal 'cymbal will be expanded by the compiler to a factory method invocation: Symbol("cymbal")
val s = Symbol("cymbal") // same as 'cymbal but '<identifier> has been deprecated
val name = s.name

// String interpolation
val str1 = s"The name of the symbol is $name"
val str2 = s"The answer is ${6 * 7}." // use {} for expressions

println("With\\\\escape! sum=${4+5}")
// The raw string interpolator behaves like s, except it does not recognize character literal escape sequences
println(raw"No\\\\escape! sum=${4+5}")

// f: <expression>[%formatter] using syntax from java.util.Formatter. defaults to %s
val str3 = "Pi"
f"$str3 is approximately ${math.Pi}%.5f"

// Prefix operators, only these identifiers are allowed: +, -, !, and ~.
val expr1 = -2
val expr1Equivalent = 2.unary_-

// Postfix operators, method that takes no arguments. NOT recommended. must explicitly enable
import scala.language.postfixOps
val expr2 = "Hello World!".toLowerCase
val expr2Equivalent = "Hello World 2!" toLowerCase

// Infix operators, methods that take a single arguement
val expr3 = "Hello".concat(" World!")
val expr3Equivalent = "Hello" concat " World!"

// ==: calls <leftOperand>.equals if leftOperand is not null
// eq and ne: compares by reference, only apply to objects that directly map to Java objects, e.g. can be used on Int
val list1 = List(1, 2)
val list2 = List(1, 2)
val expr4: Boolean = list1.eq(list2)  // false
val expr4Equivalent: Boolean = list1 eq list2 // false
val expr5 = list1.equals(list2 ) // true, List.equals compares by value
val expr5Equivalent = list1 == list2 // true

val temp = null
val expr6 = (null == temp)

val expr7 = null == "Hi" // ok as Scala takes case of null check
//val expr8 = null.equals("Hi") // throws NullPointerException

val expr9 = () == Unit // true In Scala, () is Unit