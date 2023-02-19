ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.seeta"
ThisBuild / resolvers += "Apache Snapshot Repo" at "https://repository.apache.org/content/groups/snapshots/"

lazy val root = (project in file("."))
  .settings(
    name := "pekko-persistence-test",
    libraryDependencies ++= Dependencies.allDependencies
  )
