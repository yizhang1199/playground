def f(): Int = try { return 1 } finally { return 2 } // will return 2
val callF = f
// values generated in finally are dropped
def g(): Int = try 1 finally 2 // will return 1
val callG = g