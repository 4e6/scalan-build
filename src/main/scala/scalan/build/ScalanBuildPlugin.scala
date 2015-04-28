package scalan.build

import sbt._, Keys._

object ScalanBuildPlugin extends AutoPlugin {
  import ScalanBuild._

  override def trigger = allRequirements

  object autoImport extends BuildKeys
  import autoImport._

  private def scalanSettings = Seq(
    gitRevision := ScalanBuild.gitRev,
    commands ++= Seq(snapshotRevision)
  )

  private def scalaSettings = Seq(
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xlint"),
    autoAPIMappings := true
  )

  override lazy val projectSettings = scalaSettings ++ scalanSettings
}

object ScalanBuild {

  def snapshotRevision = Command.command("snapshotRevision") { state =>
    val extracted = Project extract state
    val aggregates = aggregate(state, withCurrent = true)
    val snapshotVersion = mkSnapshotVersion(state, gitRev)

    state.log.info(s"Set version to $snapshotVersion")

    val settings = (Seq.empty[Def.Setting[String]] /: aggregates) { (s, ref) =>
      s :+ (version in ref := snapshotVersion)
    }

    extracted.append(settings, state)
  }

  def gitRev: String = {
    "git rev-parse --short HEAD".lines_!.head
  }

  def aggregate(state: State, withCurrent: Boolean): Seq[ProjectRef] = {
    val x = Project.extract(state); import x._
    val aggs = currentProject.aggregate
    if (withCurrent) currentRef +: aggs else aggs
  }

  private def mkSnapshotVersion(state: State, rev: String): String = {
    val x = Project.extract(state); import x._
    val version = get(Keys.version)
    if (get(isSnapshot)) s"$version-$rev"
    else version
  }
}
