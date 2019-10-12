// https://www.youtube.com/watch?v=bVTUCzJ2-aE&list=PLTeAcbOFvqaEk53dCLBDtHp_dADAFkpO0&index=7&t=0s

// "Call-by-Value": function arguement evaluation is done only once
// "Call-by-Name": function arguement evaluation is delayed until it's used in the function.
// "Call-by-Name" could cause the same argument to be evaluated more than once.

// notNeeded will be referenced using "Call by Value" or "Call by Name" depending on ...
def loopTest(a: Int, notNeeded: Int) : Int = a
// notUsed will be referenced using "Call by Name"
def saveTest(a: Int, notUsed: => Int) : Int = a

// val is a special case of def, e.g. in a class val x = 1 will behave as if a method x has been defined. There is one major difference:
// val -> evaluated once during initialization
// def -> evaluated each time it's referenced

// The following are syntactically correct
def x1 = 1
def x2: Int = 2
def x3(): Int = 3
def x4() = 4
def defLoop : Int = defLoop;
def x5 = defLoop;

x1
x2
x3
x4
//loopTest(86, defLoop)  // results in java.lang.StackOverflowError
//loopTest(defLoop, 87)  // results in java.lang.StackOverflowError
saveTest(88, defLoop)

// The following are syntactically correct
val val1 = 1
val val2: Int = 2
val valLoop: Int = valLoop
val valLoop2 = valLoop

val1
val2
loopTest(98, valLoop)
loopTest(valLoop, 99) // evaluates to 0.  Scala optimization???
saveTest(100, valLoop)

// variables are referenced using "Call by Name", and mutable
var var1 = 1
var1 =11
var var2: Int = 2
var2 = 22
var varLoop: Int = varLoop
var varLoop2 = varLoop

loopTest(101, varLoop) // ok, since varLoop won't be evaluated until it's referenced in loopTest
loopTest(varLoop, 99) // evaluates to 0.  Scala optimization???
saveTest(102, varLoop)