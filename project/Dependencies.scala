import sbt._

object Dependencies {
  private val akkaVersion  = "2.6.19"
  private val pekkoVersion = "0.0.0+26546-767209a8-SNAPSHOT"

  lazy private val compileDependencies = Seq(
    "org.iq80.leveldb"          % "leveldb"                     % "0.7",
    "org.fusesource.leveldbjni" % "leveldbjni-all"              % "1.8",
    "ch.qos.logback"            % "logback-classic"             % "1.2.11",
    "com.typesafe.akka"        %% "akka-persistence-typed"      % akkaVersion,
    "com.typesafe.akka"        %% "akka-serialization-jackson"  % akkaVersion,
    "org.apache.pekko"         %% "pekko-serialization-jackson" % pekkoVersion,
    "org.apache.pekko"         %% "pekko-persistence-typed"     % pekkoVersion
  )

  lazy private val testDependencies = Seq(
    "org.scalatest"     %% "scalatest"                 % "3.2.15"     % "test",
    "com.typesafe.akka" %% "akka-persistence-testkit"  % akkaVersion  % "test",
    "org.apache.pekko"  %% "pekko-persistence-testkit" % pekkoVersion % "test"
  )

  lazy val allDependencies: Seq[ModuleID] = compileDependencies ++ testDependencies
}
