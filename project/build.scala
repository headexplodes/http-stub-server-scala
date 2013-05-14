import sbt._
import sbt.Keys._

object Dependencies {

  val unfiltered = Seq(
    "net.databinder" %% "unfiltered" % "0.6.8" withSources (),
    "net.databinder" %% "unfiltered-filter" % "0.6.8" withSources (),
    "net.databinder" %% "unfiltered-netty" % "0.6.8" withSources (),
    "net.databinder" %% "unfiltered-netty-server" % "0.6.8" withSources ()
    //"net.databinder" %% "unfiltered-scalate" % "0.6.8" withSources (),
    //"net.databinder" %% "unfiltered-json" % "0.6.8" withSources ()
  )

  lazy val runtime = Seq(
  //  "ch.qos.logback" % "logback-classic" % "0.9.25" withSources (),
  //  "org.clapper" %% "grizzled-slf4j" % "0.6.6",
  //  "org.slf4j" % "jcl-over-slf4j" % "1.6.2" withSources (),
    "org.apache.commons" % "commons-lang3" % "3.1"
  )

  lazy val compile = Seq(
    "org.scala-sbt" % "sbt_2.9.1" % "0.11.3" % "compile"
  )

  lazy val test = Seq(
    //"org.scalatest" %% "scalatest" % "1.8" % "test" withSources(),
    //"junit" % "junit" % "4.10" % "test" withSources()
  )

  lazy val all = unfiltered ++ runtime /*++ compile*/ ++ test

}

object AppBuild extends Build {

  val buildName = "http-stub-server"
  val buildOrganization = "com.dz"
  val buildVersion = "0.1"
  val buildScalaVersion = "2.10.1"

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion
  )

  lazy val projectSettings = (
    buildSettings
    ++ Seq(libraryDependencies ++= Dependencies.all)
    ++ Seq(mainClass := Some("com.dz.stubby.Main")))

  lazy val project = Project(buildName, file("."), settings = projectSettings)

}
