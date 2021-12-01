// Copyright © 2017-2021 UKG Inc. <https://www.ukg.com>

name := "surge-js"

version := "0.1"

ThisBuild / scalaVersion := "2.13.5"

publish / skip := true

fork := true

lazy val app = (project in file("modules/app"))
  .settings(
    libraryDependencies ++= Seq(
      // "org.graalvm.sdk" % "graal-sdk" % "20.0.0",
      // "org.graalvm.truffle" % "truffle-api" % "20.0.0",
      // "org.graalvm.js" % "js" % "20.0.0",
      // "com.lihaoyi" %% "os-lib" % "0.7.1",
      "com.ukg" %% "surge-engine-command-scaladsl" % "0.5.44-SNAPSHOT",
      "com.typesafe.play" %% "play-json" % "2.9.2"
    ),
    publish / skip := true
  )
  .enablePlugins(JavaServerAppPackaging)
