package scalan.build

import sbt._, Keys._

object ScalanBuildPlugin extends AutoPlugin {
  import ScalanBuild._

  override def trigger = allRequirements

  lazy val baseScalanBuildSettings: Seq[Def.Setting[_]] = Seq(
    commands ++= Seq(versionSnapshot)
  )

  override lazy val projectSettings = baseScalanBuildSettings
}

object ScalanBuild {

  def versionSnapshot = Command.command("versionSnapshot") { state =>
    val extracted = Project extract state
    val aggregates = aggregate(state)
    val newVersion = mkSnapshotVersion(state, gitRevParse)

    state.log.info(s"Set version to $newVersion")

    val settings = (Seq.empty[Def.Setting[String]] /: aggregates) { (s, ref) =>
      s :+ (version in ref := newVersion)
    }

    extracted.append(settings, state)
  }

  def gitRevParse: String = {
    "git rev-parse --short HEAD".lines_!.head
  }

  def aggregate(state: State, withCurrent: Boolean = true): Seq[ProjectRef] = {
    val x = Project.extract(state); import x._
    val aggs = currentProject.aggregate
    if (withCurrent) currentRef +: aggs else aggs
  }

  private def mkSnapshotVersion(state: State, rev: String): String = {
    val x = Project.extract(state); import x._
    val versionValue = get(version)
    val versionClear =
      if (get(isSnapshot)) versionValue.dropRight("-SNAPSHOT".length)
      else versionValue
    s"$versionClear-snapshot-$rev"
  }

}
