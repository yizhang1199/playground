
/**
 * SBT keys are always scoped by the following 3 axes, with default for each axis.
 *
 * project / Config / intask / key
 *
 * The project axis can also be set to ThisBuild, which means the “entire build”, so a setting
 * applies to [all subprojects in] the entire build rather than a single project.
 * If a key that is scoped to a particular subproject is not found, sbt will look for it in ThisBuild
 * as a fallback.  Using the mechanism, we can define a build-level default setting for frequently
 * used keys such as version, scalaVersion, and organization.
 */
ThisBuild / version := "0.1"
ThisBuild / sbtVersion := "1.3.3"
ThisBuild / scalaVersion := "2.13.1"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    scalaTest % Test
  ),
  scalacOptions ++= Seq( // https://docs.scala-lang.org/overviews/compiler-options/index.html
    "-feature",
    "-deprecation",
    "-unchecked",
    "-encoding", "utf8", // Option and arguments on same line
    "-Xfatal-warnings",  // New lines for each options
    "-opt:unreachable-code,simplify-jumps"
  )
)

lazy val `scala-hello-world` = sbt.project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "scala-hello-world"
  )
  .aggregate(`rock-the-jvm-scala-advanced`)

lazy val `rock-the-jvm-scala-advanced` = sbt.project
  .in(file("rock-the-jvm-scala-advanced"))
  .settings(commonSettings)
  .settings(
    name := "rock-the-jvm-scala-advanced"
  )

lazy val exercism = sbt.project
  .settings(commonSettings)
  .settings(
    name := "exercism"
  )
