package exercises.implicits

import exercises.implicits.Tv.On

/**
 * See https://pedrorijo.com/blog/typesafe-builder/
 */
object TypeSafeBuilder extends App {

  Tv[On].turnOff
  // Tv[On].turnOn // compilation error instead of runtime
}

object Tv {
  sealed trait State
  sealed trait On extends State
  sealed trait Off extends State
}

case class Tv[S <: Tv.State](){
  def turnOn(implicit ev: S =:= Tv.Off) = Tv[Tv.On]()
  def turnOff(implicit ev: S =:= Tv.On) = Tv[Tv.Off]()
}

