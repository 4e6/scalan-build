sbtPlugin := true

name := "scalan-build"

organization := "com.huawei.scalan"

version := "0.1.5-SNAPSHOT"

licenses := Seq("Apache 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

scalacOptions := Seq(
  "-encoding", "UTF-8",
  "-feature", "-deprecation", "-unchecked")

publishMavenStyle := true
