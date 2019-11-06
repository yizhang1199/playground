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
  def onEarth(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Earth)

  def onMercury(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Mercury)

  def onVenus(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Venus)

  def onMars(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Mars)

  def onJupiter(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Jupiter)

  def onSaturn(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Saturn)

  def onUranus(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Uranus)

  def onNeptune(ageInSeconds: Double): Double = toEarthAgeInSeconds(ageInSeconds, Neptune)

  private def toEarthAgeInSeconds(ageInSeconds: Double, planet: Planet): Double = {
    ageInSeconds / (planet.orbitalPeriod * Earth.orbitalPeriodInSeconds)
  }
}

sealed trait Planet {
  /**
   * Always use def, not val, in a trait for abstract members.
   *
   * Also, use parentheses if the method changes state; otherwise don’t.  But should a trait not know if
   * an abstract member will change state during implementation?
   */
  def orbitalPeriod: Double
}

case object Earth extends Planet {
  val orbitalPeriodInSeconds: Double = 31557600
  override val orbitalPeriod: Double = 1.0
}

case object Mercury extends Planet {
  override val orbitalPeriod: Double = 0.2408467
}

case object Venus extends Planet {
  override val orbitalPeriod: Double = 0.61519726
}

case object Mars extends Planet {
  override val orbitalPeriod: Double = 1.8808158
}

case object Jupiter extends Planet {
  override val orbitalPeriod: Double = 11.862615
}

case object Saturn extends Planet {
  override val orbitalPeriod: Double = 29.447498
}

case object Uranus extends Planet {
  override val orbitalPeriod: Double = 84.016846
}

case object Neptune extends Planet {
  override val orbitalPeriod: Double = 164.79132
}