package scalan.build

import sbt._

trait BuildKeys {
  lazy val gitRevision = taskKey[String]("Git revision")
}

object BuildKeys extends BuildKeys
