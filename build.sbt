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
    publish / skip := true,
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
      case "application.conf"                            => MergeStrategy.concat
      case "unwanted.txt" => MergeStrategy.discard
      case x if Assembly.isConfigFile(x) =>
        MergeStrategy.concat
      case PathList("META-INF", xs @ _*) =>
        (xs map { _.toLowerCase }) match {
          case ("manifest.mf" :: Nil) | ("index.list" :: Nil) |
              ("dependencies" :: Nil) =>
            MergeStrategy.discard
          case ps @ (x :: xs)
              if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
            MergeStrategy.discard
          case "plexus" :: xs =>
            MergeStrategy.discard
          case "services" :: xs =>
            MergeStrategy.filterDistinctLines
          case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
            MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.first // Changed deduplicate to first
        }
      case PathList(_*) => MergeStrategy.first
      // case x =>
      //   val oldStrategy = (assemblyMergeStrategy in assembly).value
      //   oldStrategy(x)
    }
  )
  .enablePlugins(JavaServerAppPackaging)
