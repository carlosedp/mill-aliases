package com.carlosedp.aliases

import mill._
import mill.define.ExternalModule
import mill.eval.Evaluator

import Discover._
import AliasRunner._

/**
 * Aliases module
 *
 * This trait defines the aliases existing in the project
 */
trait Aliases extends Module {
  type alias = Seq[String]
  def alias(xs: String*) = Seq(xs: _*)
}

/**
 * Alias module
 *
 * @param name
 *   Alias name
 * @param module
 *   Module which contains the alias
 * @param tasks
 *   Tasks to be executed
 */
private case class Alias(
  name:   String,
  module: Module,
  tasks:  Seq[String],
)

object AliasesModule extends ExternalModule {

  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]

  /**
   * List all aliases
   *
   * @param ev
   * @return
   */
  def list(ev: Evaluator) = T.command {
    Console.err.println("Use './mill run [alias]'.");
    Console.out.println("Available aliases:")

    // Print all aliases to the console
    val aliases = getAllAliases(ev)
    Console.out.println(
      "-----------------┰-------------------┰----------------------------------------------------------------------------------"
    )
    Console.out.println("Alias            | Module            | Command(s) ")
    Console.out.println(
      "-----------------╀-------------------╀----------------------------------------------------------------------------------"
    )
    aliases.foreach(x =>
      Console.out.println(
        s"${x.name.padTo(15, ' ')}  |  ${x.module.toString.padTo(15, ' ')}  |  (${x.tasks.mkString(", ")})"
      )
    )
    Console.out.println(
      "-----------------┴-------------------┴----------------------------------------------------------------------------------"
    )
    Console.out.println("")

  }

  /**
   * Run an alias
   *
   * @param ev
   *   Evaluator
   * @param alias
   *   Alias name
   * @return
   */
  def run(ev: Evaluator, alias: String) = T.command {
    // Get the existing aliases
    val aliases = getAllAliases(ev)

    // Check if the alias exists and get it's tasks
    aliases.map(_.name).contains(alias) match {
      case true =>
        val tasks    = aliases.filter(_.name == alias).head.tasks
        val runTasks = tasks.flatMap(x => Seq(x, "+")).flatMap(_.split("\\s+")).init
        Console.out.println(s"Running alias $alias")
        aliasRunner(
          ev,
          runTasks,
        )
      case false =>
        Console.err.println(s"Alias '$alias' not found.")
        Console.err.println(s"")
        printHelp()
        sys.exit(1)
    }
  }

  def help() = T.command {
    Console.err.println("--------------------")
    Console.err.println("Mill Aliases Plugin")
    Console.err.println("--------------------")
    printHelp()
  }

  private def printHelp() = {
    Console.err.println("The plugin allows you to define aliases for mill tasks.")
    Console.err.println(
      "The aliases are defined in an object extending `Aliases` in the build.sc file at the root level in the following format:"
    )
    Console.err.println("""
                          |  object MyAliases extends Aliases {
                          |    def testall     = alias("__.test")
                          |    def compileall  = alias("__.compile")
                          |    def testcompall = alias("__.compile", "__.test")
                          |  }
    """.stripMargin)
    Console.err.println("Aliases can be defines for one or multiple tasks which will be run in sequence.")
    Console.err.println("The aliases can be run with './mill Alias/run [alias]'.")
    Console.err.println("To list all aliases: './mill Alias/list'")
  }

  private def aliasModules(ev: Evaluator): Seq[Module with Aliases] =
    ev.rootModule.millInternal.modules.collect { case m: Aliases => m }

  private def aliasMethods(module: Module): Seq[String] =
    // modules.flatMap(_.getClass().getMethods())
    module
      .getClass()
      .getMethods()
      .map(_.getName())
      .filterNot(
        Seq(
          "alias",
          "wait",
          "equals",
          "toString",
          "hashCode",
          "getClass",
          "notify",
          "notifyAll",
          "mill$define$Module$$millModuleDirectChildrenImpl",
          "millModuleDirectChildren",
          "millModuleExternal",
          "millModuleShared",
          "millInternal",
          "millModuleSegments",
          "millSourcePath",
          "millModuleBasePath",
          "mill$moduledefs$Cacher$$cacherLazyMap",
          "millOuterCtx",
          "cachedTarget",
        ).contains(_)
      ).toSeq

  private def aliasCommands(module: Module, alias: String): Seq[String] =
    module.getClass().getDeclaredMethod(alias).invoke(module).asInstanceOf[Seq[String]]

  private def getAllAliases(ev: Evaluator): Seq[Alias] =
    aliasModules(ev).flatMap { module =>
      aliasMethods(module).map { alias =>
        Alias(
          name = alias,
          module = module,
          tasks = aliasCommands(module, alias),
        )
      }
    }

}
