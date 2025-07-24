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
  def list(ev: Evaluator): Unit =
    Console.err.println("Use './mill Alias/run [alias]'.");
    Console.out.println("Available aliases:")
    Console.out.println()

    // Get all aliases for reference checking
    val allAliases = getAllAliases(ev)
    val aliasNames = allAliases.map(_.name).toSet

    if allAliases.isEmpty then
      Console.out.println("  No aliases defined.")
      Console.out.println()
    else
      // Calculate dynamic column widths
      val aliasMaxWidth    = math.max(15, allAliases.map(_.name.length).max + 2)
      val moduleMaxWidth   = math.max(15, allAliases.map(_.module.toString.length).max + 2)
      val commandsMaxWidth = 60 // Fixed reasonable width for commands

      // ANSI color codes
      val cyan   = "\u001b[36m"
      val green  = "\u001b[32m"
      val yellow = "\u001b[33m"
      val reset  = "\u001b[0m"
      val bold   = "\u001b[1m"

      // Generate table borders
      val topBorder    = s"┌${"─" * aliasMaxWidth}┬${"─" * moduleMaxWidth}┬${"─" * commandsMaxWidth}┐"
      val midBorder    = s"├${"─" * aliasMaxWidth}┼${"─" * moduleMaxWidth}┼${"─" * commandsMaxWidth}┤"
      val bottomBorder = s"└${"─" * aliasMaxWidth}┴${"─" * moduleMaxWidth}┴${"─" * commandsMaxWidth}┘"

      // Print table header
      Console.out.println(topBorder)
      Console.out.println(
        s"│ ${bold}Alias${reset}${" " * (aliasMaxWidth - 6)}│ ${bold}Module${reset}${" " * (moduleMaxWidth - 7)}│ ${bold}Command(s)${reset}${" " * (commandsMaxWidth - 11)}│"
      )
      Console.out.println(midBorder)

      // Print aliases
      allAliases.sortBy(_.name).foreach { x =>
        // Mark tasks that are aliases with colored symbols
        val markedTasks = x.tasks.map { task =>
          val taskName = task.split("\\s+").head
          if aliasNames.contains(taskName) then s"${cyan}${task}${yellow}→${reset}" else task
        }

        val commandsText = markedTasks.mkString(", ")

        // Calculate visual length (without ANSI codes) for proper padding
        val visualLength = commandsText.replaceAll("\u001b\\[[0-9;]*m", "").length

        val truncatedCommands = if visualLength > commandsMaxWidth - 3 then
          // For truncation, we need to be more careful with color codes
          val plainText      = commandsText.replaceAll("\u001b\\[[0-9;]*m", "")
          val truncatedPlain = plainText.take(commandsMaxWidth - 6) + "..."
          truncatedPlain
        else
          commandsText

        // Recalculate visual length after potential truncation
        val finalVisualLength = truncatedCommands.replaceAll("\u001b\\[[0-9;]*m", "").length

        val aliasName  = s"${green}${x.name}${reset}"
        val moduleName = x.module.toString

        // Calculate padding based on visual length, not string length with ANSI codes
        val commandsPadding = commandsMaxWidth - finalVisualLength - 1
        val padding         = if commandsPadding > 0 then " " * commandsPadding else ""

        Console.out.println(
          s"│ ${aliasName}${" " * (aliasMaxWidth - x.name.length - 1)}│ ${moduleName}${" " * (moduleMaxWidth - moduleName.length - 1)}│ ${truncatedCommands}${padding}│"
        )
      }
      Console.out.println(bottomBorder)
      Console.out.println()
      Console.out.println(s"${yellow}Legend:${reset}")
      Console.out.println(s"  ${cyan}Colored tasks${yellow}→${reset} Reference other aliases")
      Console.out.println(s"  ${green}Green names${reset}     Alias names")
      Console.out.println()
    end if
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
    val cyan    = "\u001b[36m"
    val green   = "\u001b[32m"
    val yellow  = "\u001b[33m"
    val red     = "\u001b[31m"
    val magenta = "\u001b[35m"
    val reset   = "\u001b[0m"
    val bold    = "\u001b[1m"

    if visitedAliases.contains(alias.name) then
      Console.out.println(
        s"${bold}${magenta}Circular dependency detected: alias '${alias.name}' references itself${reset}"
      )
      Result.Failure(s"Circular dependency detected: alias '${alias.name}' references itself")
    else
      def runSteps(tasks: Seq[String], indent: Int): Result[String] =
        tasks.foldLeft(Result.Success(""): Result[String]) { (acc, task) =>
          if acc.isInstanceOf[Result.Failure] then acc
          else
            val taskName = task.split("\\s+").head
            val prefix   = "  " * indent
            findAliasByName(taskName, allAliases) match
              case Some(dependentAlias) =>
                Console.out.println(s"${prefix}${bold}${cyan}↳ Running alias ${taskName}${reset}")
                runAliasWithDependenciesIndented(dependentAlias, visitedAliases + alias.name, indent + 1)
              case None =>
                Console.out.println(s"${prefix}${bold}${yellow}→ Running task ${task}${reset}")
                checkAliasTasks(ev, Alias(alias.name, alias.module, Seq(task))) match
                  case Result.Success(_) =>
                    val runTasks = Seq(task, "+").flatMap(_.split("\\s+")).init
                    aliasRunner(ev, runTasks) match
                      case s: Result.Success[?] => Result.Success(s.value.toString)
                      case f: Result.Failure    => f
                  case _ =>
                    Console.out.println(
                      s"${prefix}${bold}${magenta}Error: Task '$task' in alias '${alias.name}' is invalid${reset}"
                    )
                    Result.Failure(s"Error: Task '$task' in alias '${alias.name}' is invalid")
            end match
        } match
          case f: Result.Failure => f
          case _ => Result.Success(s"Alias ${alias.name} finished successfully")

      def runAliasWithDependenciesIndented(
          alias:          Alias,
          visitedAliases: Set[String],
          indent:         Int,
        ): Result[String] =
        val prefix = "  " * indent
        if visitedAliases.contains(alias.name) then
          Console.out.println(
            s"${prefix}${bold}${magenta}Circular dependency detected: alias '${alias.name}' references itself${reset}"
          )
          Result.Failure(s"Circular dependency detected: alias '${alias.name}' references itself")
        else
          Console.out.println(s"${prefix}${bold}${green}▶ Running alias ${alias.name}${reset}")
          runSteps(alias.tasks, indent)

      // Initial call with indent 0
      Console.out.println(s"${bold}${green}▶ Running alias ${alias.name}${reset}")
      val result = runSteps(alias.tasks, 1)
      result match
        case s: Result.Success[?] =>
          Console.out.println(s"${bold}${green}▶ Alias ${alias.name} finished successfully${reset}")
          s
        case f: Result.Failure =>
          Console.out.println(s"${bold}${red}▶ Alias ${alias.name} failed. ${f.error}${reset}")
          f
    end if
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
          |    def prepall     = alias ("mill.scalalib.scalafmt/ __.sources", "testcompall")
          |  }
        """.stripMargin)
    Console.err.println("Aliases can be defined with one or multiple tasks or other aliases which will be run in sequence.")
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
    // List of method names to exclude
    val exclude = Set(
      "alias",
      "wait",
      "equals",
      "toString",
      "hashCode",
      "getClass",
      "notify",
      "notifyAll",
      "millModuleDirectChildren",
      "millModuleExternal",
      "millModuleShared",
      "millInternal",
      "millModuleSegments",
      "millSourcePath",
      "millModuleBasePath",
      "millOuterCtx",
      "cachedTarget",
      "$anonfun$millSourcePath$1",
      "moduleNestedCtx",
      "moduleDirectChildren",
      "moduleLinearized",
      "moduleDir",
      "moduleSegments",
      "moduleInternal",
      "moduleDirJava",
      "moduleCtx",
      "cachedTask",
    )
    module.getClass.getMethods
      .iterator
      .map(_.getName)
      .filterNot(_.matches("""build_\$package_\$.*\$\$\$\$outer"""))
      .filterNot(_.matches("""mill\$.*"""))
      .filterNot(exclude)
      .toSeq
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
