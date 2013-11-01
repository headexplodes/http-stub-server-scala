import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin._


object BuildSettings {

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.dz",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.10.3",
    scalacOptions ++= Seq("-feature"),
    fork := true // working around issue where JavaScript script engine was not found in tests (sbt 0.13.0)
  )

  val publishSettings = releaseSettings ++ Seq(
    credentials += Credentials("Sonatype Nexus Repository Manager", "localhost", "deployment", "admin123"),
    publishTo := Some("releases" at "http://localhost:8081/nexus/content/repositories/releases/"),
    pomExtra :=
      <url>https://github.com/headexplodes/http-stub-server-scala</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
  )


}

object RootBuild extends Build {

  import BuildSettings._

  lazy val coreSettings = (
    buildSettings
      ++ Seq(libraryDependencies ++= Dependencies.jackson ++ Dependencies.runtime ++ Dependencies.test))

  lazy val standaloneSettings = (
    buildSettings
      ++ (libraryDependencies ++= Dependencies.all)
      ++ (mainClass := Some("com.dz.stubby.standalone.Main"))
      ++ (unmanagedResources in Compile += (baseDirectory.value / ".." / "LICENSE.txt"))
      ++ (unmanagedResources in Compile += (baseDirectory.value / ".." / "README.md"))
      ++ sbtassembly.Plugin.assemblySettings
    )

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings) aggregate(core, standalone, functionalTest)

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = coreSettings ++ publishSettings)

  lazy val standalone = Project(
    id = "standalone",
    base = file("standalone"),
    settings = standaloneSettings ++ publishSettings) dependsOn (core)

  lazy val functionalTest = Project(
    id = "functionalTest",
    base = file("functional-test"),
    settings = buildSettings)

}

object Dependencies {

  val unfiltered = Seq(
    "net.databinder" %% "unfiltered" % "0.6.8" withSources(),
    "net.databinder" %% "unfiltered-filter" % "0.6.8" withSources(),
    "net.databinder" %% "unfiltered-netty" % "0.6.8" withSources(),
    "net.databinder" %% "unfiltered-netty-server" % "0.6.8" withSources()
  )

  val jackson = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2" withSources(),
    "com.fasterxml.jackson.core" % "jackson-core" % "2.2.2" withSources(),
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2" withSources()
  )

  lazy val runtime = Seq(
    "org.apache.commons" % "commons-lang3" % "3.1",
    "commons-io" % "commons-io" % "2.4",
    "org.apache.httpcomponents" % "httpclient" % "4.2.5" withSources(),
    "com.typesafe" %% "scalalogging-log4j" % "1.0.1" withSources(),
    "org.apache.logging.log4j" % "log4j-core" % "2.0-beta3" withSources()
    //"org.mozilla" % "rhino" % "1.7R4"
  )

  lazy val test = Seq(
    "org.scalatest" %% "scalatest" % "2.0.M5b" % "test" withSources(),
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test" withSources()
  )

  lazy val all = unfiltered ++ jackson ++ runtime ++ test

}
