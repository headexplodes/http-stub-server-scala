import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.SbtPgp.PgpKeys._
import scala._
import scala.Some
import Utilities._
import sbtbuildinfo.Plugin._

object BuildSettings {

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.dividezero",
    scalaVersion := "2.10.3",
    scalacOptions ++= Seq("-feature"),
    fork := true // working around issue where JavaScript script engine was not found in tests (sbt 0.13.0)
  )

  val publishSettings = Nil 
  
  /*
  val publishSettings2 = releaseSettings ++ Seq(
    publishMavenStyle := true,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra :=
      <url>https://github.com/headexplodes/http-stub-server-scala</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>https://github.com/headexplodes/http-stub-server-scala</url>
        <connection>https://github.com/headexplodes/http-stub-server-scala.git</connection>
      </scm>
      <developers>
        <developer>
          <id>thetrav</id>
          <name>Travis Dixon</name>
          <email>the.trav@gmail.com</email>
        </developer>
        <developer>
          <id>headexplodes</id>
          <name>Robert Parsons</name>
          <email>headexplodes@gmail.com</email>
        </developer>
      </developers>,

    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,              // : ReleaseStep
      inquireVersions,                        // : ReleaseStep
      runTest,                                // : ReleaseStep
      setReleaseVersion,                      // : ReleaseStep
      commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
      tagRelease,                             // : ReleaseStep
      publishArtifacts.copy(action = publishSignedAction),
      setNextVersion,                         // : ReleaseStep
      commitNextVersion,                      // : ReleaseStep
      pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
    )
  )

  lazy val publishSignedAction = { st: State =>
    val extracted = st.extract
    val ref = extracted.get(thisProjectRef)
    extracted.runAggregated(publishSigned in Global in ref, st)
  }
  */

}

object RootBuild extends Build {

  import BuildSettings._

  lazy val coreSettings = (
    buildSettings
      ++ Seq(libraryDependencies ++= Dependencies.jackson ++ Dependencies.runtime ++ Dependencies.test))
      
  lazy val functionalTestSettings = (
    buildSettings
      ++ Seq(libraryDependencies ++= Dependencies.jackson ++ Dependencies.runtime ++ Dependencies.test ++ Dependencies.functionalTest))

  lazy val standaloneSettings = (
    buildSettings
      ++ (libraryDependencies ++= Dependencies.all)
      ++ (mainClass := Some("com.dividezero.stubby.standalone.Main"))
      ++ (unmanagedResources in Compile += (baseDirectory.value / ".." / "LICENSE.txt"))
      ++ (unmanagedResources in Compile += (baseDirectory.value / ".." / "README.md"))
      ++ sbtassembly.Plugin.assemblySettings
      ++ buildInfoSettings
      ++ Seq(
          sourceGenerators in Compile <+= buildInfo,
          buildInfoKeys := Seq[BuildInfoKey](name, version),
          buildInfoPackage := "com.dividezero.stubby"
      )
    )
   
  lazy val root = Project(
    id = "stubby-root",
    base = file("."),
    settings = buildSettings ++ publishSettings) aggregate(core, standalone, functionalTest)

  lazy val core = Project(
    id = "stubby-core",
    base = file("core"),
    settings = coreSettings ++ publishSettings)

  lazy val standalone = Project(
    id = "stubby-standalone",
    base = file("standalone"),
    settings = standaloneSettings ++ publishSettings) dependsOn (core)

  lazy val functionalTest = Project(
    id = "stubby-functionalTest",
    base = file("functional-test"),
    settings = functionalTestSettings ++ publishSettings) dependsOn (standalone)

}

object Dependencies {

  val unfiltered = Seq(
    "net.databinder" %% "unfiltered" % "0.6.8",
    "net.databinder" %% "unfiltered-filter" % "0.6.8",
    "net.databinder" %% "unfiltered-netty" % "0.6.8",
    "net.databinder" %% "unfiltered-netty-server" % "0.6.8"
  )

  val jackson = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2",
    "com.fasterxml.jackson.core" % "jackson-core" % "2.2.2",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2"
  )

  lazy val runtime = Seq(
    "org.apache.commons" % "commons-lang3" % "3.1",
    "commons-io" % "commons-io" % "2.4",
    "org.apache.httpcomponents" % "httpclient" % "4.2.5",
    "com.typesafe" %% "scalalogging-log4j" % "1.0.1",
    "org.apache.logging.log4j" % "log4j-core" % "2.0-beta3"
    //"org.mozilla" % "rhino" % "1.7R4"
  )

  lazy val test = Seq(
    // "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "org.scalatest" %% "scalatest" % "2.0" % "test",
    "junit" % "junit" % "4.11" % "test"
  )
  
  lazy val functionalTest = Seq(
    "org.scalatest" %% "scalatest" % "2.0"
  )

  lazy val all = unfiltered ++ jackson ++ runtime ++ test

}
