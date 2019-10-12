var i: Int = 3

while (i > 0) {
  println("while loop " + i)
  i = i - 1
}

i = 3
do {
  println("do while loop " + i)
  i = i - 1
} while (i > 0)

for (i <- 1 until 3) println("for loop with until: " + i)

for (i <- 1 to 3) println("for loop with to: " + i)

// for loops translates to "foreach", example:
(1 to 3).foreach(n => println("foreach: " + n))

var j = 0;
for (i <- 1 to 3; j <- "ab") println("for loop: i=" + i + ", j=" + j)

// The above for loop is equivalent to nested foreach function calls:
(1 to 3).foreach(i => {
  "ab".foreach(j => println("foreach: i=" + i + ", j=" + j))
})