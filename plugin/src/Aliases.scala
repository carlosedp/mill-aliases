package com.carlosedp.aliases

import com.carlosedp.aliases.AliasRunner.*
import mill.Module
import mill.aliasesUtil.Utils
import mill.api.{Evaluator, Result}

/**
 * Aliases module
 *
 * This trait defines the aliases existing in the project
 */
trait Aliases extends Module:
  type alias = Seq[String]
  def alias(xs: String*) = Seq(xs*)

/**
 * Alias object
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

/**
 * Aliases module
 *
 * This module is used to list and run aliases
 */
object AliasesModule {

  /**
   * List all aliases
   *
   * @param ev
   * @return
   */
  def list(ev: Evaluator) =
    Console.err.println("Use './mill Alias/run [alias]'.");
    Console.out.println("Available aliases:")

    // Get all aliases for reference checking
    val allAliases = getAllAliases(ev)
    val aliasNames = allAliases.map(_.name).toSet

    // Print all aliases to the console
    Console.out.println(
      "┌─────────────────┬─────────────────┬───────────────────────────────────────────────────────────────────────────────────"
    )
    Console.out.println("| Alias           | Module          | Command(s) ")
    Console.out.println(
      "├─────────────────┼─────────────────┼───────────────────────────────────────────────────────────────────────────────────"
    )

    allAliases.sortBy(_.name).foreach { x =>
      // Mark tasks that are aliases with *
      val markedTasks = x.tasks.map { task =>
        val taskName = task.split("\\s+").head
        if aliasNames.contains(taskName) then s"$task*" else task
      }
      Console.out.println(
        s"| ${x.name.padTo(15, ' ')} | ${x.module.toString.padTo(15, ' ')} | ${markedTasks.mkString(", ")}"
      )
    }
    Console.out.println(
      "└─────────────────┴─────────────────┴───────────────────────────────────────────────────────────────────────────────────"
    )
    Console.out.println("")
    Console.out.println("Note: Tasks marked with '*' are references to other aliases.")
    Console.out.println("")
  end list

  /**
   * Run an alias
   *
   * @param ev
   *   Evaluator
   * @param alias
   *   Alias name
   * @return
   */
  def run(ev: Evaluator, aliasName: String) =
    findAliasByName(aliasName, getAllAliases(ev)) match
      case None =>
        printHelp()
        Console.err.println(s"")
        Result.Failure(s"Alias '$aliasName' not found.")
      case Some(alias) =>
        runAliasWithDependencies(ev, alias, getAllAliases(ev), Set.empty)

  /**
   * Run an alias with support for alias dependencies (aliases referencing other
   * aliases)
   *
   * @param ev
   *   Evaluator
   * @param alias
   *   Alias to run
   * @param allAliases
   *   All available aliases in the project
   * @param visitedAliases
   *   Set of already visited aliases to prevent circular dependencies
   * @return
   */
  private def runAliasWithDependencies(
      ev:             Evaluator,
      alias:          Alias,
      allAliases:     Seq[Alias],
      visitedAliases: Set[String],
    ): Result[String] =
    // Check for circular dependencies
    if visitedAliases.contains(alias.name) then
      Result.Failure(s"Circular dependency detected: alias '${alias.name}' references itself")
    else
      val newVisitedAliases = visitedAliases + alias.name
      Console.out.println(s"Running alias ${alias.name}")

      // Process each task in the alias
      val results = alias.tasks.map { task =>
        val taskName = task.split("\\s+").head // Get the main task name (ignore arguments)

        // Check if this task is actually another alias
        findAliasByName(taskName, allAliases) match
          case Some(dependentAlias) =>
            // This is an alias reference, run it recursively
            runAliasWithDependencies(ev, dependentAlias, allAliases, newVisitedAliases)
          case None =>
            // This is a regular mill task, validate and run it
            checkAliasTasks(ev, Alias(alias.name, alias.module, Seq(task))) match
              case Result.Success(_) =>
                val runTasks = Seq(task, "+").flatMap(_.split("\\s+")).init
                aliasRunner(ev, runTasks)
              case _ =>
                Result.Failure(s"Error: Task '$task' in alias '${alias.name}' is invalid")
      }

      // Check if any task failed
      results.find(_.isInstanceOf[Result.Failure]) match
        case Some(failure) => failure.asInstanceOf[Result.Failure]
        case None          => Result.Success(s"Alias ${alias.name} finished successfully")
  end runAliasWithDependencies

  def help() =
    Console.err.println("--------------------")
    Console.err.println("Mill Aliases Plugin")
    Console.err.println("--------------------")
    printHelp()

  // --------------------------------------------------------------------------------------------------------------------

  /**
   * Prints help message to the console
   */
  private def printHelp() = {
    // format: off
    Console.err.println("The plugin allows you to define aliases for mill tasks.")
    Console.err.println("The aliases are defined in an object extending `Aliases` in the build.sc file at the root level in the following format:")
    Console.err.println(
        """
          |  object MyAliases extends Aliases {
          |    def testall     = alias("__.test")
          |    def compileall  = alias("__.compile")
          |    def testcompall = alias("__.compile", "__.test")
          |    def comptest    = alias ("compileall", "testall")
          |  }
        """.stripMargin)
    Console.err.println("Aliases can be defined for one or multiple tasks or other aliases which will be run in sequence.")
    Console.err.println("The aliases can be run with './mill Alias/run [alias]'.")
    Console.err.println("To list all aliases: './mill Alias/list'")
    // format: on
  }

  /**
   * Get all aliases in the project module
   *
   * @param module
   * @return
   *   A `Seq` of aliases
   */
  private def aliasMethods(module: Module): Seq[String] =
    val methods = module
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
          "$anonfun$millSourcePath$1",
          "mill$define$Cacher$$cacherLazyMap",
          "build_$package_$MyAliases$$$$outer",
          "mill$api$Module$_setter_$moduleLinearized_$eq",
          "mill$api$Module$$millModuleDirectChildrenImpl",
          "moduleNestedCtx",
          "moduleDirectChildren",
          "moduleLinearized",
          "moduleDir",
          "moduleSegments",
          "moduleInternal",
          "moduleDirJava",
          "mill$api$internal$Cacher$$cacherLazyMap",
          "moduleCtx",
          "cachedTask",
        ).contains(_)
      ).toSeq
    methods
  end aliasMethods

  /**
   * Get all commands for an alias in a specific module
   *
   * @param module
   * @param alias
   * @return
   *   A `Seq` of tasks
   */
  private def aliasCommands(module: Module, alias: String) =
    module.getClass().getDeclaredMethods().toIndexedSeq.flatMap { m =>
      if m.getName() == alias then
        m.invoke(module) match
          case tasks: Seq[?] =>
            tasks.map(_.toString)
          case task: String =>
            Seq(task)
          case _ =>
            Seq.empty
      else
        Seq.empty
    }

  /**
   * Get all aliases in the project
   *
   * @param ev
   *   Evaluator
   * @return
   *   A `Seq` of [[Aliases]]
   */
  private def getAllAliases(ev: Evaluator): Seq[Alias] =
    Utils.aliasModules(ev).flatMap { module =>
      aliasMethods(module).map { alias =>
        Alias(
          name = alias,
          module = module,
          tasks = aliasCommands(module, alias),
        )
      }
    }

  /**
   * Find an alias by name
   *
   * @param name
   *   Alias name
   * @param aliases
   *   A `Seq` of `[[Alias]]`es
   * @return
   */
  private def findAliasByName(name: String, aliases: Seq[Alias]): Option[Alias] =
    aliases.find(_.name == name)

  /**
   * Check if any task in alias is invalid
   *
   * @param ev
   *   Evaluator
   * @param alias
   *   Alias to check
   * @return
   */
  private def checkAliasTasks(ev: Evaluator, alias: Alias) =
    val filteredTasks = alias.tasks.map(_.split("\\s+").head)
    Utils.taskResolver(ev, filteredTasks)
}
