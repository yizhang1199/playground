name := "scala-hello-world"
version := "0.1"
sbtVersion := "1.3.3"
scalaVersion := "2.13.1"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

lazy val `scala-hello-world` = sbt.project
  .in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )