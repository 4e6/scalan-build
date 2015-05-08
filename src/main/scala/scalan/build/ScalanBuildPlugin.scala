package scalan.build

import sbt._, Keys._

object ScalanBuildPlugin extends AutoPlugin {
  import ScalanBuild._

  override def trigger = allRequirements

  object autoImport extends BuildKeys
  import autoImport._

  private def scalanSettings = Seq(
    gitRevision := ScalanBuild.gitRev.get,
    commands ++= Seq(versionFixed)
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

  type Revision = String

  val RevLength = 7

  def versionFixed = Command.command("versionFixed") { state =>
    val extracted = Project extract state
    val aggregates = aggregate(state, withCurrent = true)
    val revision = envRev orElse gitRev map { format(_) }
    val fixedVersion = fixVersion(state, revision)

    state.log.info(s"Set version to $fixedVersion")

    val settings = (Seq.empty[Def.Setting[String]] /: aggregates) { (s, ref) =>
      s :+ (version in ref := fixedVersion)
    }

    extracted.append(settings, state)
  }

  def aggregate(state: State, withCurrent: Boolean): Seq[ProjectRef] = {
    val x = Project.extract(state); import x._
    val aggs = currentProject.aggregate
    if (withCurrent) currentRef +: aggs else aggs
  }

  def gitRev: Option[Revision] =
    util.Try(s"git rev-parse HEAD".lines_!.head).toOption

  def envRev: Option[Revision] =
    sys.env.get("VCS_REVISION")

  def format(rev: Revision, length: Int = RevLength): Revision =
    rev take length

  def fixVersion(state: State, revision: Option[Revision]): String = {
    val x = Project.extract(state); import x._
    val version = get(Keys.version)
    revision.fold(version) { rev =>
      if (get(isSnapshot)) s"$version-$rev" else version
    }
  }
}
