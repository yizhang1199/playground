package exercism

/**
 * https://exercism.io/my/solutions/d57bf0905f984e5c8050df48821e85cd
 *
 * Given an age in seconds, calculate how old someone would be on:
 *
 * Mercury: orbital period 0.2408467 Earth years
 * Venus: orbital period 0.61519726 Earth years
 * Earth: orbital period 1.0 Earth years, 365.25 Earth days, or 31557600 seconds
 * Mars: orbital period 1.8808158 Earth years
 * Jupiter: orbital period 11.862615 Earth years
 * Saturn: orbital period 29.447498 Earth years
 * Uranus: orbital period 84.016846 Earth years
 * Neptune: orbital period 164.79132 Earth years
 */
object SpaceAge {
  def onEarth(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Earth)

  def onMercury(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Mercury)

  def onVenus(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Venus)

  def onMars(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Mars)

  def onJupiter(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Jupiter)

  def onSaturn(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Saturn)

  def onUranus(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Uranus)

  def onNeptune(ageInSeconds: Double): Double = ageInSeconds / orbitalPeriodInEarthSeconds(Neptune)

  private val orbitalPeriodInEarthSeconds: Map[Planet, Double] = Map(
    Earth -> 1.0,
    Mercury -> 0.2408467,
    Venus -> 0.61519726,
    Mars -> 1.8808158,
    Jupiter -> 11.862615,
    Saturn -> 29.447498,
    Uranus -> 84.016846,
    Neptune -> 164.79132
  ).map {
    keyValue => keyValue.copy(_2 = 31557600 * keyValue._2.asInstanceOf[Double])
  }
}

sealed trait Planet extends Serializable

case object Earth extends Planet

case object Mercury extends Planet

case object Venus extends Planet

case object Mars extends Planet

case object Jupiter extends Planet

case object Saturn extends Planet

case object Uranus extends Planet

case object Neptune extends Planet

