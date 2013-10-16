import sbtrelease.ReleasePlugin._

organization := "com.github.jarlakxen"

name := "scalatra-rest"

version := "1.0-SNAPSHOT"

crossScalaVersions := Seq("2.9.3", "2.10.3")

crossVersion := CrossVersion.full

publishMavenStyle := true

publishArtifact in Test := false

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

ivyLoggingLevel := UpdateLogging.Full

scalacOptions <++= scalaVersion map { v =>
  if (v.startsWith("2.10"))
    Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps", "-language:existentials" )
  else
    Seq("-unchecked", "-deprecation")
}

seq(releaseSettings: _*)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.0"  withSources() withJavadoc(),
  "org.scalatra" %% "scalatra" % "2.2.1" withSources() withJavadoc(),
  "org.scalatra" %% "scalatra-auth" % "2.2.1" withSources() withJavadoc(),
  "org.scalatra" %% "scalatra-scalate" % "2.2.1" withSources() withJavadoc(),
  "org.scalatra" %% "scalatra-json" % "2.2.1" withSources(),
  "org.json4s" %% "json4s-jackson" % "3.2.5" withSources(),
  "org.json4s" %% "json4s-ext" % "3.2.5" withSources(),
  "ch.qos.logback" % "logback-classic" % "1.0.13" % "runtime",
  "com.github.nscala-time" %% "nscala-time" % "0.6.0" withSources(),
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided" withSources() withJavadoc(),
  "org.specs2" %% "specs2" % "2.2.3" % "test" withSources() withJavadoc(),
  "org.scalatra" %% "scalatra-specs2" % "2.2.1" % "test" withSources() withJavadoc(),
  "junit" % "junit" % "4.11" % "test"
)

pomExtra := (
  <url>https://github.com/Jarlakxen/scalatra-rest</url>
  <licenses>
    <license>
      <name>GPL v2</name>
      <url>https://github.com/Jarlakxen/scalatra-rest/blob/master/LICENSE</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:Jarlakxen/scalatra-rest.git</url>
    <connection>scm:git:git@github.com:Jarlakxen/scalatra-rest.git</connection>
  </scm>
  <developers>
    <developer>
      <id>Jarlakxen</id>
      <name>Facundo Viale</name>
      <url>https://github.com/Jarlakxen/scalatra-rest</url>
    </developer>
  </developers>
)

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

publishTo <<= version { v =>
  val nexus = "http://oss.sonatype.org/"
  if (v.endsWith("-SNAPSHOT"))
    Some("sonatype-nexus-snapshots" at nexus + "content/repositories/snapshots/")
  else
    Some("sonatype-nexus-staging" at nexus + "service/local/staging/deploy/maven2/")
}
