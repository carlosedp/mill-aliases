import $ivy.`io.chris-kipp::mill-ci-release::0.1.9`

import mill._
import mill.scalalib._
import mill.scalalib.scalafmt._
import mill.scalalib.publish._
import mill.scalalib.api.ZincWorkerUtil._

import de.tobiasroeser.mill.vcs.version.VcsVersion
import io.kipp.mill.ci.release._

val millVersions = Seq("0.10.12", "0.11.0")
val scala213     = "2.13.11"
val pluginName   = "mill-aliases"

object plugin extends Cross[Plugin](millVersions)
trait Plugin  extends Cross.Module[String]
  with ScalaModule
  with Publish
  with ScalafmtModule {

  val millVersion           = crossValue
  override def scalaVersion = scala213
  override def artifactName = s"${pluginName}_mill${scalaNativeBinaryVersion(millVersion)}"

  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-scalalib:${millVersion}"
  )

  override def scalacOptions = Seq("-deprecation", "-feature")

  override def sources = T.sources {
    super.sources() ++ Seq(
      millSourcePath / s"src-mill${millVersion.split('.').take(2).mkString(".")}"
    ).map(PathRef(_))
  }
}

trait Publish extends CiReleaseModule {
  override def pomSettings = PomSettings(
    description =
      "A Mill plugin to allow the creation of aliases to common-use tasks.",
    organization = "com.carlosedp",
    url = "https://github.com/carlosedp/mill-aliases",
    licenses = Seq(License.MIT),
    versionControl = VersionControl
      .github(owner = "carlosedp", repo = "mill-aliases"),
    developers = Seq(
      Developer(
        "carlosedp",
        "Carlos Eduardo de Paula",
        "https://github.com/carlosedp",
      )
    ),
  )
  override def publishVersion: T[String] = T {
    val isTag = T.ctx().env.get("GITHUB_REF").exists(_.startsWith("refs/tags"))
    val state = VcsVersion.vcsState()
    if (state.commitsSinceLastTag == 0 && isTag) {
      state.stripV(state.lastTag.get)
    } else {
      val v = if (state.lastTag.isEmpty) { Array("0", "0", "0") }
      else { state.stripV(state.lastTag.get).split('.') }
      s"${v(0)}.${(v(1).toInt) + 1}-SNAPSHOT"
    }
  }
  override def sonatypeHost = Some(SonatypeHost.s01)
}
