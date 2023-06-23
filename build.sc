// import $ivy.`com.goyeau::mill-scalafix::0.2.11`
import $ivy.`io.chris-kipp::mill-ci-release::0.1.9`

import mill._
import mill.scalalib._
import mill.scalalib.scalafmt._
import mill.scalalib.publish._
import mill.scalalib.api.ZincWorkerUtil
import mill.scalalib.api.ZincWorkerUtil.scalaNativeBinaryVersion

// import com.goyeau.mill.scalafix.ScalafixModule
import de.tobiasroeser.mill.vcs.version.VcsVersion
import io.kipp.mill.ci.release.CiReleaseModule
import io.kipp.mill.ci.release.SonatypeHost

val millVersions = Seq("0.10.12", "0.11.0")
val scala213     = "2.13.11"
val pluginName   = "mill-aliases"

object plugin extends Cross[Plugin](millVersions)
trait Plugin  extends Cross.Module[String]
  with ScalaModule
  with CiReleaseModule
  // with ScalafixModule
  with ScalafmtModule {

  val millVersion           = crossValue
  override def scalaVersion = scala213
  override def artifactName = s"${pluginName}_mill${scalaNativeBinaryVersion(millVersion)}"

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
  override def sonatypeHost = Some(SonatypeHost.s01)

  override def compileIvyDeps = super.compileIvyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-scalalib:${millVersion}"
  )

  override def scalacOptions = Seq("-deprecation", "-feature")

  override def sources = T.sources {
    super.sources() ++ Seq(
      millSourcePath / s"src-mill${millVersion.split('.').take(2).mkString(".")}"
    ).map(PathRef(_))
  }

  // override def scalafixScalaBinaryVersion =
  //   ZincWorkerUtil.scalaBinaryVersion(scala213)

  // override def scalafixIvyDeps = Agg(
  //   ivy"com.github.liancheng::organize-imports:0.6.0"
  // )
}
