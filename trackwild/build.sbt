name := """TrackWild"""
organization := "NateHan"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

resolvers ++= Seq(
  "webjars"    at "http://webjars.github.com/m2"
)

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  guice, filters,
  "org.webjars" % "bootstrap" % "3.3.7",
  evolutions,
  //DB drivers
  javaJdbc,
  "org.postgresql" % "postgresql" % "42.1.1"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "NateHan.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "NateHan.binders._"
