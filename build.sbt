import sbtrelease.ReleasePlugin._

organization := "com.github.jarlakxen"

name := "scalatra-rest"

version := "1.3"

crossScalaVersions := Seq("2.10.2")

scalaVersion <<= (crossScalaVersions) { versions => versions.head }

publishMavenStyle := true

publishArtifact in Test := false

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

scalacOptions <++= scalaVersion map { v =>
  if (v.startsWith("2.10"))
  Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps", "-language:existentials" )
  else
  Seq("-unchecked", "-deprecation")
}

seq(releaseSettings: _*)

libraryDependencies <++= (scalaVersion) { (version) =>
  Seq(
    "org.scala-lang" % "scala-reflect" % "2.10.0",
    "org.scalatra" %% "scalatra" % "2.2.1",
    "org.scalatra" %% "scalatra-auth" % "2.2.1",
    "org.scalatra" %% "scalatra-scalate" % "2.2.1",
    "org.scalatra" %% "scalatra-json" % "2.2.1",
    "org.json4s" %% "json4s-jackson" % "3.2.5",
    "org.json4s" %% "json4s-ext" % "3.2.5",
    "ch.qos.logback" % "logback-classic" % "1.0.13" % "runtime",
    "com.github.nscala-time" %% "nscala-time" % "0.6.0",
    "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
    "org.specs2" %% "specs2" % "2.2.3" % "test",
    "org.scalatra" %% "scalatra-specs2" % "2.2.1" % "test",
    "junit" % "junit" % "4.11" % "test"
    )
}

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
