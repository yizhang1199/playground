package exercism

import scala.collection.immutable.TreeMap

/**
 * https://exercism.io/my/solutions/cf20638e7c524e8fbf2265f9013b9552
 *
 * Given students' names along with the grade that they are in, create a roster for the school.
 *
 * In the end, you should be able to:
 *
 * Add a student's name to the roster for a grade
 * "Add Jim to grade 2."
 * "OK."
 * Get a list of all students enrolled in a grade
 * "Which students are in grade 2?"
 * "We've only got Jim just now."
 * Get a sorted list of all students in all grades. Grades should sort as 1, 2, 3, etc., and students within a grade should be sorted alphabetically by name.
 * "Who all is enrolled in school right now?"
 * "Grade 1: Anna, Barb, and Charlie. Grade 2: Alex, Peter, and Zoe. Grade 3…"
 *
 * Note that all our students only have one name. (It's a small town, what do you want?)
 */
class School {
  type DB = Map[Int, Seq[String]]
  private var database: DB = TreeMap[Int, Seq[String]]()

  def add(name: String, g: Int): Unit = database += g -> (database.getOrElse(g, Vector()) :+ name) // Vector has constant append

  def db: DB = database

  def grade(g: Int): Seq[String] = db.getOrElse(g, List())

  def sorted: DB =
    database.keySet.foldLeft(Map[Int, Seq[String]]()) {
      (acc, key) => acc + (key -> database(key).sorted)
    }
}