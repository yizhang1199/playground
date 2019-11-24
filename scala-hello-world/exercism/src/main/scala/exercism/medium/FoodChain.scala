package exercism.medium

import scala.annotation.tailrec

/**
 * Generate the lyrics of the song 'I Know an Old Lady Who Swallowed a Fly'.
 *
 * While you could copy/paste the lyrics, or read them from a file, this problem is much more interesting if
 * you approach it algorithmically.
 *
 * This is a cumulative song of unknown origin. This is one of many common variants.
 *
 * I know an old lady who swallowed a fly.
 * I don't know why she swallowed the fly. Perhaps she'll die.
 *
 * I know an old lady who swallowed a spider.
 * It wriggled and jiggled and tickled inside her.
 * She swallowed the spider to catch the fly.
 * I don't know why she swallowed the fly. Perhaps she'll die.
 *
 * I know an old lady who swallowed a bird.
 * How absurd to swallow a bird!
 * She swallowed the bird to catch the spider that wriggled and jiggled and tickled inside her.
 * She swallowed the spider to catch the fly.
 * I don't know why she swallowed the fly. Perhaps she'll die.
 *
 * I know an old lady who swallowed a cat.
 * Imagine that, to swallow a cat!
 * She swallowed the cat to catch the bird.
 * She swallowed the bird to catch the spider that wriggled and jiggled and tickled inside her.
 * She swallowed the spider to catch the fly.
 * I don't know why she swallowed the fly. Perhaps she'll die.
 *
 * I know an old lady who swallowed a dog.
 * What a hog, to swallow a dog!
 * She swallowed the dog to catch the cat.
 * She swallowed the cat to catch the bird.
 * She swallowed the bird to catch the spider that wriggled and jiggled and tickled inside her.
 * She swallowed the spider to catch the fly.
 * I don't know why she swallowed the fly. Perhaps she'll die.
 *
 * I know an old lady who swallowed a goat.
 * Just opened her throat and swallowed a goat!
 * She swallowed the goat to catch the dog.
 * She swallowed the dog to catch the cat.
 * She swallowed the cat to catch the bird.
 * She swallowed the bird to catch the spider that wriggled and jiggled and tickled inside her.
 * She swallowed the spider to catch the fly.
 * I don't know why she swallowed the fly. Perhaps she'll die.
 *
 * I know an old lady who swallowed a cow.
 * I don't know how she swallowed a cow!
 * She swallowed the cow to catch the goat.
 * She swallowed the goat to catch the dog.
 * She swallowed the dog to catch the cat.
 * She swallowed the cat to catch the bird.
 * She swallowed the bird to catch the spider that wriggled and jiggled and tickled inside her.
 * She swallowed the spider to catch the fly.
 * I don't know why she swallowed the fly. Perhaps she'll die.
 *
 * I know an old lady who swallowed a horse.
 * She's dead, of course!
 */
object FoodChain {
  def recite(startParagraph: Int, endParagraph: Int): String = {
    require(startParagraph <= endParagraph &&
      animals.get(startParagraph).isDefined && animals.get(endParagraph).isDefined)

    val lyrics = for ( i <- startParagraph to endParagraph ) yield generate(i)
    lyrics mkString ""
  }

  import Animal._

  private val animals: Map[Int, Animal] = Map(
    1 -> fly,
    2 -> spider,
    3 -> bird,
    4 -> cat,
    5 -> dog,
    6 -> goat,
    7 -> cow,
    8 -> horse
  )

  private def generate(key: Int): String = {
    require(animals.get(key).isDefined)
    var phrases = Vector(s"I know an old lady who swallowed a ${animals(key).name}.", animals(key).phrase)
    if (animals(key).myPrey.isDefined) {
      phrases = phrases :++ generatePreyPhrases(animals(key))
      phrases = phrases :+ animals(1).phrase
    }
    phrases.mkString("", "\n", "\n\n")
  }

  @tailrec
  private def generatePreyPhrases(animal: Animal, phrases: Seq[String] = Seq()): Seq[String] = {
    if (animal.myPrey.isDefined) {
      val prey = animal.myPrey.get
      val phrase = s"She swallowed the ${animal.name} to catch the ${prey.name}${prey.action.getOrElse(".")}"
      generatePreyPhrases(prey, phrase +: phrases)
    } else phrases.reverse
  }
}

case class Animal(name: String, phrase: String, myPrey: Option[Animal] = None, action: Option[String] = None)

object Animal {
  val fly = Animal("fly", "I don't know why she swallowed the fly. Perhaps she'll die.")
  val spider = Animal("spider", "It wriggled and jiggled and tickled inside her.", Some(fly),
    Some(" that wriggled and jiggled and tickled inside her."))
  val bird = Animal("bird", "How absurd to swallow a bird!", Some(spider))
  val cat = Animal("cat", "Imagine that, to swallow a cat!", Some(bird))
  val dog = Animal("dog", "What a hog, to swallow a dog!", Some(cat))
  val goat = Animal("goat", "Just opened her throat and swallowed a goat!", Some(dog))
  val cow = Animal("cow", "I don't know how she swallowed a cow!", Some(goat))
  val horse = Animal("horse", "She's dead, of course!")
}