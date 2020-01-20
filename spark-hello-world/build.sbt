import sbt.Keys.dependencyOverrides

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
ThisBuild / sbtVersion := "1.3.7"
ThisBuild / scalaVersion := "2.12.8" // "2.12.8" "2.11.12"

lazy val sparkVersion = "2.4.4"
lazy val deltaLakeVersion = "0.5.0"

// The following are added due to jar conflicts with spark
lazy val json4sVersion = "3.5.5" // Spark 2.4.4 uses json4s 3.5.3. Other versions will cause compilation problems.
//lazy val jacksonVersion = "2.6.7" // spark 2.4.4 uses jackson 2.6.7
//lazy val jacksonDatabindVersion = "2.6.7.3"
//lazy val scalaXmlVersion = "1.2.0"
lazy val guavaVersion = "14.0.1"

//lazy val kafkaStreamsVersion = "2.4.0"

lazy val scalaLoggingVersion = "3.9.2"
lazy val logbackVersion = "1.2.3"

// http://www.scalactic.org/
lazy val scalacticVersion = "3.1.0"

// testing jar versions
lazy val scalaTestVersion = "3.1.0"
lazy val mockitoScalaVersion = "1.10.5"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    // commons
    "org.scalactic" %% "scalactic" % scalacticVersion,
    //"commons-validator" % "commons-validator" % "1.6",
    //"javax.mail" % "mail" % "1.4.7",

    // spark & databricks
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
    "io.delta" %% "delta-core" % deltaLakeVersion,

    // kafka
    //"org.apache.kafka" %% "kafka-streams-scala" % kafkaStreamsVersion,

    "com.google.guava" % "guava" % guavaVersion,

    // logging
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,

    // testing
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.mockito" %% "mockito-scala" % mockitoScalaVersion % Test
  ),
  scalacOptions ++= Seq( // https://docs.scala-lang.org/overviews/compiler-options/index.html
    "-feature",
    "-deprecation",
    "-unchecked",
    "-encoding", "utf8", // Option and arguments on same line
    "-Xfatal-warnings" // New lines for each options
    //"-opt:unreachable-code,simplify-jumps" // not supported on scala 2.11
  ),
  dependencyOverrides ++= {
    Seq(
      // Pin these to versions required by Spark to avoid java.lang.NoSuchMethodError or Incompatible version errors
//      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
//      "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
//      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion,
      "org.json4s" %% "json4s-native" % json4sVersion,
      "org.json4s" %% "json4s-ast" % json4sVersion,
      "org.json4s" %% "json4s-core" % json4sVersion,
      "org.json4s" %% "json4s-jackson" % json4sVersion,
      "org.json4s" %% "json4s-scalap" % json4sVersion,
      "com.google.guava" % "guava" % guavaVersion
    )
  }
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