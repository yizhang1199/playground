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

  import Animal.{Animals, StartAnimal}

  def recite(startParagraph: Int, endParagraph: Int): String = {
    require(startParagraph <= endParagraph &&
      Animals.get(startParagraph).isDefined && Animals.get(endParagraph).isDefined)

    val lyrics = (startParagraph to endParagraph).map(generate)
    lyrics mkString ""
  }

  private def generate(key: Int): String = {
    require(Animals.get(key).isDefined)

    val animal = Animals(key)
    var phrases = Vector(s"I know an old lady who swallowed a ${animal.name}.", animal.phrase)

    if (animal.myPrey.isDefined) {
      phrases = phrases :++ generatePreyPhrases(animal)
      phrases = phrases :+ StartAnimal.phrase
    }

    phrases.mkString("", "\n", "\n\n")
  }

  @tailrec
  private def generatePreyPhrases(animal: Animal, phrases: Seq[String] = Seq()): Seq[String] = {
    animal.myPrey match {
      case Some(prey) =>
        val phrase = s"She swallowed the ${animal.name} to catch the ${prey.name}${prey.action.getOrElse(".")}"
        generatePreyPhrases(prey, phrase +: phrases)
      case None =>
        phrases.reverse
    }
  }
}

private[medium] case class Animal(name: String, phrase: String,
                                  myPrey: Option[Animal] = None,
                                  action: Option[String] = None)

private[medium] object Animal {
  private[medium] val Fly = Animal("fly", "I don't know why she swallowed the fly. Perhaps she'll die.")
  private[medium] val Spider = Animal("spider", "It wriggled and jiggled and tickled inside her.", Some(Fly),
    Some(" that wriggled and jiggled and tickled inside her."))
  private[medium] val Bird = Animal("bird", "How absurd to swallow a bird!", Some(Spider))
  private[medium] val Cat = Animal("cat", "Imagine that, to swallow a cat!", Some(Bird))
  private[medium] val Dog = Animal("dog", "What a hog, to swallow a dog!", Some(Cat))
  private[medium] val Goat = Animal("goat", "Just opened her throat and swallowed a goat!", Some(Dog))
  private[medium] val Cow = Animal("cow", "I don't know how she swallowed a cow!", Some(Goat))
  private[medium] val Horse = Animal("horse", "She's dead, of course!")

  private[medium] val Animals: Map[Int, Animal] = Map(
    1 -> Fly,
    2 -> Spider,
    3 -> Bird,
    4 -> Cat,
    5 -> Dog,
    6 -> Goat,
    7 -> Cow,
    8 -> Horse
  )

  private[medium] val StartAnimal = Animals(1)
}