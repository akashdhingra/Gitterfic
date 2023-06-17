lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """KP_G02""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      guice,
      javaWs,
      ehcache,
      // Test Database
      "com.h2database" % "h2" % "1.4.199",
      // Testing libraries for dealing with CompletionStage...
      "org.assertj" % "assertj-core" % "3.14.0" % Test,
      "org.awaitility" % "awaitility" % "4.0.1" % Test,
      "org.codehaus.jackson" % "jackson-core-asl" % "1.9.13",
      "org.riversun" % "java-promise" % "1.1.0",
      "org.mockito" % "mockito-core" % "3.6.0",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.14" % Test

    ),
    javacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-parameters",
      "-Xlint:unchecked",
      "-Xlint:deprecation",
      "-Werror"
    ),
    // Make verbose tests
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
  )
