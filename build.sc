import mill._
import mill.scalalib._
import mill.scalalib.scalafmt._
import mill.scalalib.publish._
import mill.scalalib.api.ZincWorkerUtil._

import $ivy.`com.carlosedp::mill-aliases::0.5.0`
import com.carlosedp.aliases._
import $ivy.`com.goyeau::mill-scalafix::0.4.2`
import com.goyeau.mill.scalafix.ScalafixModule
import $ivy.`io.chris-kipp::mill-ci-release::0.3.0`
import io.kipp.mill.ci.release._
import de.tobiasroeser.mill.vcs.version.VcsVersion

object versions {
    val pluginName   = "mill-aliases"
    val millVersions = Seq("0.10.12", "0.11.0") // scala-steward:off
    val scala213     = "2.13.15"
}

object plugin extends Cross[Plugin](versions.millVersions)
trait Plugin  extends Cross.Module[String]
    with ScalaModule
    with Publish
    with ScalafixModule
    with ScalafmtModule {

    val millVersion            = crossValue
    def scalaVersion           = versions.scala213
    def artifactName           = s"${versions.pluginName}_mill${scalaNativeBinaryVersion(millVersion)}"
    override def scalacOptions = Seq("-Ywarn-unused", "-deprecation")

    def compileIvyDeps = super.compileIvyDeps() ++ Agg(
        ivy"com.lihaoyi::mill-scalalib:${millVersion}",
        ivy"org.scala-lang:scala-reflect:${scalaVersion()}",
    )

    def sources = T.sources {
        super.sources() ++ Seq(
            millSourcePath / s"src-mill${scalaNativeBinaryVersion(millVersion)}"
        ).map(PathRef(_))
    }
}

trait Publish extends CiReleaseModule {
    def pomSettings = PomSettings(
        description = "A Mill plugin to allow the creation of aliases to common-use tasks.",
        organization = "com.carlosedp",
        url = "https://github.com/carlosedp/mill-aliases",
        licenses = Seq(License.MIT),
        versionControl = VersionControl.github(owner = "carlosedp", repo = "mill-aliases"),
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
    def sonatypeHost = Some(SonatypeHost.s01)
}

object MyAliases extends Aliases {
    def fmt      = alias("mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources")
    def checkfmt = alias("mill.scalalib.scalafmt.ScalafmtModule/checkFormatAll __.sources")
    def lint     = alias("mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources", "__.fix")
    def deps     = alias("mill.scalalib.Dependency/showUpdates")
    def pub      = alias("io.kipp.mill.ci.release.ReleaseModule/publishAll")
    def publocal = alias("__.publishLocal")
    def testall  = alias("__.test")
}
