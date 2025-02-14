val scala2_12 = "2.12.18"
val scala2_13 = "2.13.11"
val scala3 = "3.3.0"
val scalaVersions = List(scala2_12, scala2_13, scala3)

ThisBuild / dynverSonatypeSnapshots := true

lazy val commonSettings = Seq(
  organization := "com.github.pjfanning",
  versionScheme := Some("early-semver"),
  licenses += ("Apache-2.0", new URL("https://github.com/pjfanning/pekko-http-session/blob/main/LICENSE")),
  startYear := Some(2023),
  homepage := Some(url("https://github.com/pjfanning/pekko-http-session")),
  developers := Developer("softwaremill", "SoftwareMill", "info@softwaremill.com", url("https://softwaremill.com")) ::
    Developer("pjfanning", "PJ Fanning", "", url("https://github.com/pjfanning")) :: Nil,
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/pjfanning/pekko-http-session.git"),
      connection = "scm:git:git@github.com:pjfanning/pekko-http-session.git"
    )
  ),
  publishTo := sonatypePublishToBundle.value
)

val pekkoHttpVersion = "1.0.0"
val pekkoStreamsVersion = "1.0.1"
val scalaJava8CompatVersion = "1.0.2"
val json4sVersion = "4.0.6"
val jacksonVersion = "2.14.3"
val pekkoStreamsProvided = "org.apache.pekko" %% "pekko-stream" % pekkoStreamsVersion % "provided"
val pekkoStreamsTestkit = "org.apache.pekko" %% "pekko-stream-testkit" % pekkoStreamsVersion % "test"

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.16" % "test"

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publish / skip := true, name := "pekko-http-session-root", scalaVersion := scala2_13)
  .aggregate(core.projectRefs ++ jwt.projectRefs ++ example.projectRefs ++ javaTests.projectRefs: _*)

lazy val core = (projectMatrix in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "pekko-http-session-core",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
      "org.scala-lang.modules" %% "scala-java8-compat" % scalaJava8CompatVersion,
      pekkoStreamsProvided,
      "org.apache.pekko" %% "pekko-http-testkit" % pekkoHttpVersion % "test",
      pekkoStreamsTestkit,
      "org.scalacheck" %% "scalacheck" % "1.15.4" % "test",
      scalaTest
    )
  )
  .jvmPlatform(scalaVersions = scalaVersions)

lazy val jwt = (projectMatrix in file("jwt"))
  .settings(commonSettings: _*)
  .settings(
    name := "pekko-http-session-jwt",
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-jackson" % json4sVersion,
      "org.json4s" %% "json4s-ast" % json4sVersion,
      "org.json4s" %% "json4s-core" % json4sVersion,
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
      pekkoStreamsProvided,
      scalaTest
    ),
    // generating docs for 2.13 causes an error: "not found: type DefaultFormats$"
    Compile / doc / sources := Seq.empty
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(core)

lazy val example = (projectMatrix in file("example"))
  .settings(commonSettings: _*)
  .settings(
    publishArtifact := false,
    libraryDependencies ++= Seq(
      pekkoStreamsProvided,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "ch.qos.logback" % "logback-classic" % "1.2.12",
      "org.json4s" %% "json4s-ext" % json4sVersion
    )
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(core, jwt)

lazy val javaTests = (projectMatrix in file("javaTests"))
  .settings(commonSettings: _*)
  .settings(
    name := "javaTests",
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.JUnit, "-a")), // required for javadsl JUnit tests
    crossPaths := false, // https://github.com/sbt/junit-interface/issues/35
    publishArtifact := false,
    libraryDependencies ++= Seq(
      pekkoStreamsProvided,
      "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
      "org.scala-lang.modules" %% "scala-java8-compat" % scalaJava8CompatVersion,
      "org.apache.pekko" %% "pekko-http-testkit" % pekkoHttpVersion % "test",
      pekkoStreamsTestkit,
      "junit" % "junit" % "4.13.2" % "test",
      "com.github.sbt" % "junit-interface" % "0.13.3" % "test",
      scalaTest
    )
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .dependsOn(core, jwt)
