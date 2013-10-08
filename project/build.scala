import sbt._
import Keys._

import com.typesafe.sbteclipse.plugin.EclipsePlugin._


object ScalatraRest extends Build {

    val Organization = "com.github.jarlakxen"
    val Name = "Scalatra Rest"
    val Version = "1.0-SNAPSHOT"
    val ScalaVersion = "2.10.2"
    val ScalatraVersion = "2.2.1"

    lazy val project = Project (
        "scalatra-rest",
        file("."),
        settings =  Defaults.defaultSettings ++
                    Seq(
                        organization := Organization,
                        name := Name,
                        version := Version,
                        scalaVersion := ScalaVersion,
                        scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature", "-language:implicitConversions", "-language:postfixOps", "-language:existentials" ),
                        EclipseKeys.withSource := true,
                        EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala) ++
                    Seq(resolvers ++= Seq(Classpaths.typesafeReleases)) ++

                    Seq(
                        libraryDependencies ++= Seq(

                            "org.scala-lang" % "scala-reflect" % ScalaVersion,

                            // Scalatra
                            "org.scalatra" %% "scalatra" % ScalatraVersion withSources() withJavadoc(),
                            "org.scalatra" %% "scalatra-auth" % ScalatraVersion withSources() withJavadoc(),
                            "org.scalatra" %% "scalatra-scalate" % ScalatraVersion withSources() withJavadoc(),
                            "org.scalatra" %% "scalatra-json" % ScalatraVersion withSources(),
                            "org.json4s" %% "json4s-jackson" % "3.2.5" withSources(),
                            "org.json4s" %% "json4s-ext" % "3.2.5" withSources(),
                            "ch.qos.logback" % "logback-classic" % "1.0.9" % "runtime",

                            // Utils
                            "joda-time" % "joda-time" % "2.3" withSources(),

                            "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided" withSources() withJavadoc(),

                            // Testing
                            "org.specs2" %% "specs2" % "2.2.3" % "test" withSources() withJavadoc(),
                            "junit" % "junit" % "4.8.1" % "test"
                        )
                    )
        ) 
}