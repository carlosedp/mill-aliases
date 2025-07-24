package com.carlosedp.aliases

import mill.testkit.IntegrationTester
import utest.*

object AliasesSpec extends TestSuite:
  val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
  val tester         = new IntegrationTester(
    daemonMode = true,
    workspaceSourcePath = resourceFolder,
    millExecutable = os.Path(sys.env("MILL_EXECUTABLE_PATH")),
  )
  val pluginVersion = sys.env.getOrElse("TEST_PLUGIN_VERSION", "0.0.0-SNAPSHOT")
  val testRepo      = sys.env.getOrElse("MILL_USER_TEST_REPO", "/tmp")
  println("Running tests in dir: " + tester.workspacePath)
  println(s"Using plugin version: $pluginVersion")
  println(s"Test plugin published to repo: $testRepo")
  // Replace plugin version and repo with the one from the test environment
  tester.modifyFile(tester.workspacePath / "build.mill", _.replace("PLUGIN_VERSION", pluginVersion))
  tester.modifyFile(tester.workspacePath / "build.mill", _.replace("LOCAL_REPO_PATH", testRepo))

  // Run the tests
  val tests: Tests = Tests:
    println("Initializing integration tests")

    test("print help"):
      val res = tester.eval("Alias/help")
      assert(res.isSuccess)
      assert(res.err.contains("Mill Aliases Plugin"))
      assert(res.err.contains("testall"))
      assert(res.err.contains("To list all aliases"))

    test("list aliases"):
      val res = tester.eval("Alias/list")
      assert(res.isSuccess)

      // Expected aliases and their commands for MyAliases object
      val expectedAliases = Seq(
        ("fmt", "mill.scalalib.scalafmt/ __.sources"),
        ("checkfmt", "mill.scalalib.scalafmt/checkFormatAll __.sources"),
        ("lint", "mill.scalalib.scalafmt/ __.sources, __.fix"),
        ("deps", "mill.scalalib.Dependency/showUpdates"),
        ("fmtDeps", "fmt→, deps→"),
        ("lintComp", "lint→, __.compile"),
        ("circularAlias", "circularAlias→"),
      )
      val expectedGroup = "MyAliases"

      // Check each expected alias line exists in output
      expectedAliases.foreach { case (alias, command) =>
        val regex = raw"""$alias\s+\│\s+$expectedGroup\s+\│\s+$command""".r
        assert(res.out.linesIterator.exists(regex.findFirstIn(_).isDefined))
      }

      // Check Aliases2 object alias
      val alias2Regex = raw"""someAlias\s+\│\s+Aliases2\s+\│\s+__.compile""".r
      assert(res.out.linesIterator.exists(alias2Regex.findFirstIn(_).isDefined))

    test("alias not found"):
      val res = tester.eval("Alias/run nonExistentAlias")
      assert(!res.isSuccess)

    test("circular alias"):
      val res = tester.eval("Alias/run circularAlias")
      assert(!res.isSuccess)

    test("run alias fmt"):
      // Add some spaces before the println command to make sure format works
      val scalafile = tester.workspacePath / "foo/src/main.scala"
      tester.modifyFile(scalafile, _.replace(" println", "    println"))
      assert(os.read(scalafile).contains("def hello() =    println"))
      // Run the fmt alias
      val res = tester.eval(Seq("Alias/run", "fmt"))
      // Check if the alias ran successfully
      assert(res.isSuccess)
      assert(res.out.contains("Running alias fmt"))
      assert(res.out.contains("Running task mill.scalalib.scalafmt/ __.sources"))
      assert(os.read(scalafile).contains("def hello() = println"))
      assert(res.out.contains("Alias fmt finished successfully"))

    test("run composed alias fmtDeps"):
      val scalafile = tester.workspacePath / "foo/src/main.scala"
      tester.modifyFile(scalafile, _.replace(" println", "    println"))
      assert(os.read(scalafile).contains("def hello() =    println"))

      // Run the fmtDeps alias
      val res = tester.eval(Seq("Alias/run", "fmtDeps"))
      // Check if the alias ran successfully
      assert(res.isSuccess)
      assert(res.out.contains("Running alias fmtDeps"))
      assert(res.out.contains("Running alias fmt"))
      assert(res.err.contains("Formatting 1 Scala sources"))
      assert(res.out.contains("Running alias deps"))
      assert(res.out.contains("No dependency updates found for foo"))
      assert(res.out.contains("Alias fmtDeps finished successfully"))
      // Check if the file was formatted
      assert(os.read(scalafile).contains("def hello() = println"))
end AliasesSpec
