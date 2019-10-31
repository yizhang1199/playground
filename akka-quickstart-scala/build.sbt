name := "akka-quickstart-scala"
organization := "com.intuit"

version := "1.0"

scalaVersion := "2.12.10"

sbtVersion := "1.3.3"

lazy val akkaVersion = "2.5.26"

lazy val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
lazy val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.28"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

lazy val `akka-hello-world` = sbt.project
  .in(file("."))
  .aggregate(iot)

lazy val iot = (project in file("iot-sample"))
  .settings(
    name := "iot-sample",
    libraryDependencies ++= Seq(
      akkaActor,
      slf4jSimple,
      akkaTestKit % Test,
      scalaTest % Test
    )
  )
