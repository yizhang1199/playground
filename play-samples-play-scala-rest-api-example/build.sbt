import sbt.Keys._

lazy val akkaVersion = "2.5.26"

lazy val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
lazy val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.28"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
lazy val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3"
lazy val logback = "net.logstash.logback" % "logstash-logback-encoder" % "5.2"

lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, Common)
  .settings(
    name := "play-scala-rest-api-example",
    version := "2.7.x",
    scalaVersion := "2.13.0",
    libraryDependencies ++= Seq(
      guice, // from PlayImport.scala
      "org.joda" % "joda-convert" % "2.1.2",
      logback,
      "io.lemonlabs" %% "scala-uri" % "1.4.10",
      "net.codingwell" %% "scala-guice" % "4.2.5",
      akkaActor,
      scalaTestPlusPlay % Test
    ),
  )

lazy val gatlingVersion = "3.1.3"
lazy val gatling = (project in file("gatling"))
  .enablePlugins(GatlingPlugin)
  .settings(
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Test,
      "io.gatling" % "gatling-test-framework" % gatlingVersion % Test
    )
  )

// Documentation for this project:
//    sbt "project docs" "~ paradox"
//    open docs/target/paradox/site/index.html
lazy val docs = (project in file("docs")).enablePlugins(ParadoxPlugin).
  settings(
    scalaVersion := "2.13.0",
    paradoxProperties += ("download_url" -> "https://example.lightbend.com/v1/download/play-rest-api")
  )
