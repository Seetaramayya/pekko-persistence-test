import sbt._

object Dependencies {
  private val akkaVersion  = "2.6.19"
  private val pekkoVersion = "0.0.0+26546-767209a8-SNAPSHOT"
  // This version is published locally. It is not available snapshot repository
  private val pekkoLocalVersion = "0.0.0+948-19ae6a10-SNAPSHOT"

  lazy private val compileDependencies = Seq(
    "org.iq80.leveldb"          % "leveldb"                     % "0.7",
    "org.fusesource.leveldbjni" % "leveldbjni-all"              % "1.8",
    "ch.qos.logback"            % "logback-classic"             % "1.2.11",
    "com.typesafe.slick"       %% "slick"                       % "3.3.3",
    "com.typesafe.slick"       %% "slick-hikaricp"              % "3.3.3",
    "com.lightbend.akka"       %% "akka-persistence-jdbc"       % "4.0.0",
    "org.apache.pekko"         %% "pekko-persistence-jdbc"      % pekkoLocalVersion,
    "org.postgresql"            % "postgresql"                  % "42.5.1",
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
