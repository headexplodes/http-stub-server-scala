import sbt._
import sbt.Keys._

object BuildSettings {
  
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.dz",
    version := "0.1",
    scalaVersion := "2.10.2",
    scalacOptions ++= Seq()
  )
  
}

object RootBuild extends Build {

  import BuildSettings._

  lazy val coreSettings = (
    buildSettings
    ++ Seq(libraryDependencies ++= Dependencies.jackson ++ Dependencies.runtime ++ Dependencies.test))
  
  lazy val standaloneSettings = (
    buildSettings
    ++ Seq(libraryDependencies ++= Dependencies.all)
    ++ Seq(mainClass := Some("com.dz.stubby.Main")))
    
  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings) aggregate (core, standalone, functionalTest)

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = coreSettings)

  lazy val standalone = Project(
    id = "standalone",
    base = file("standalone"),
    settings = standaloneSettings) dependsOn (core)

  lazy val functionalTest = Project(
    id = "functionalTest",
    base = file("functional-test"),
    settings = buildSettings)
    
}

object Dependencies {

  val unfiltered = Seq(
    "net.databinder" %% "unfiltered" % "0.6.8" withSources (),
    "net.databinder" %% "unfiltered-filter" % "0.6.8" withSources (),
    "net.databinder" %% "unfiltered-netty" % "0.6.8" withSources (),
    "net.databinder" %% "unfiltered-netty-server" % "0.6.8" withSources ()
  )
  
  val jackson = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2" withSources(),
    "com.fasterxml.jackson.core" % "jackson-core" % "2.2.2" withSources(),
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2" withSources()    
  )

  lazy val runtime = Seq(
    "org.apache.commons" % "commons-lang3" % "3.1",
    "org.apache.commons" % "commons-io" % "1.3.2",
    "org.apache.httpcomponents" % "httpclient" % "4.2.5" withSources(),
    "com.typesafe" %% "scalalogging-log4j" % "1.0.1" withSources(),
    "org.apache.logging.log4j" % "log4j-core" % "2.0-beta3" withSources()
  )

  lazy val test = Seq(
    "org.scalatest" %% "scalatest" % "2.0.M5b" % "test" withSources(),
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test" withSources()
  )

  lazy val all = unfiltered ++ jackson ++ runtime ++ test

}
