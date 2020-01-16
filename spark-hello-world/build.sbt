
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
ThisBuild / scalaVersion := "2.12.8"

val sparkVersion = "2.4.4"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
lazy val slf4sApi = "ch.timo-schmid" %% "slf4s-api" % "1.7.26" //"org.slf4s" %% "slf4s-api" % "1.7.25"
lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
lazy val deltaLakeCore = "io.delta" %% "delta-core" % "0.5.0"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    scalaTest % Test,
    slf4sApi,
    // spark
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
    "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
    // commons
    "commons-validator" % "commons-validator" % "1.6",
    "javax.mail" % "mail" % "1.4.7",
    // Databricks
    deltaLakeCore
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

lazy val `spark-hello-world` = sbt.project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "spark-hello-world"
  )
  .aggregate(`udemy-spark-with-scala`)

lazy val `udemy-spark-with-scala` = sbt.project
  .in(file("udemy-spark-with-scala"))
  .settings(commonSettings)
  .settings(
    name := "udemy-spark-with-scala"
  )