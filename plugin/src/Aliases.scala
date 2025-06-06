package com.carlosedp.aliases

import com.carlosedp.aliases.AliasRunner._
import mill._
import mill.api.Result
import mill.eval.Evaluator

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
    def list(ev: Evaluator) = {
        Console.err.println("Use './mill run [alias]'.");
        Console.out.println("Available aliases:")

        // Print all aliases to the console
        Console.out.println(
            "┌─────────────────┬─────────────────┬───────────────────────────────────────────────────────────────────────────────────"
        )
        Console.out.println("| Alias           | Module          | Command(s) ")
        Console.out.println(
            "├─────────────────┼─────────────────┼───────────────────────────────────────────────────────────────────────────────────"
        )

        getAllAliases(ev).sortBy(_.name).foreach(x =>
            Console.out.println(
                s"| ${x.name.padTo(15, ' ')} | ${x.module.toString.padTo(15, ' ')} | ${x.tasks.mkString(", ")}"
            )
        )
        Console.out.println(
            "└─────────────────┴─────────────────┴───────────────────────────────────────────────────────────────────────────────────"
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
    def run(ev: Evaluator, aliasName: String) =
        findAliasByName(aliasName, getAllAliases(ev)) match {
            case None =>
                printHelp()
                Console.err.println(s"")
                Result.Failure(s"Alias '$aliasName' not found.")
            case Some(alias) =>
                checkAliasTasks(ev, alias) match {
                    case Result.Success(_) =>
                        val runTasks = alias.tasks.flatMap(x => Seq(x, "+")).flatMap(_.split("\\s+")).init
                        Console.out.println(s"Running alias $aliasName")
                        aliasRunner(ev, runTasks) match {
                            case Result.Success(value) =>
                                Result.Success(s"Alias $aliasName finished successfully")
                            case Result.Failure(msg, value) =>
                                Result.Failure(msg)
                            case _ => Result.Failure(s"Alias $aliasName failed with unknown error")
                        }
                    case _ =>
                        Result.Failure(
                            s"Error: A task defined in alias '${alias.name}' is invalid: (${alias.tasks.mkString(", ")})"
                        )
                }
        }

    def help() = {
        Console.err.println("--------------------")
        Console.err.println("Mill Aliases Plugin")
        Console.err.println("--------------------")
        printHelp()
    }

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
          |  }
        """.stripMargin)
    Console.err.println("Aliases can be defines for one or multiple tasks which will be run in sequence.")
    Console.err.println("The aliases can be run with './mill Alias/run [alias]'.")
    Console.err.println("To list all aliases: './mill Alias/list'")
    // format: on
    }

    /**
     * Get all modules that extend [[Aliases]] in the project
     *
     * @param ev
     *   Evaluator
     * @return
     *   A `Seq` of [[Aliases]] modules
     */
    private def aliasModules(ev: Evaluator): Seq[Aliases] =
        ev.rootModule.millInternal.modules.collect { case m: Aliases => m }

    /**
     * Get all aliases in the project module
     *
     * @param module
     * @return
     *   A `Seq` of aliases
     */
    private def aliasMethods(module: Module): Seq[String] =
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
                    "$anonfun$millSourcePath$1",
                    "mill$define$Cacher$$cacherLazyMap",
                ).contains(_)
            ).toSeq

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
            if (m.getName() == alias) {
                m.invoke(module) match {
                    case tasks: Seq[?] =>
                        tasks.map(_.toString)
                    case task: String =>
                        Seq(task)
                    case _ =>
                        Seq.empty
                }
            } else {
                Seq.empty
            }
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
        aliasModules(ev).flatMap { module =>
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
    private def checkAliasTasks(ev: Evaluator, alias: Alias) = {
        val filteredTasks = alias.tasks.map(_.split("\\s+").head)
        Utils.taskResolver(ev, filteredTasks)
    }
}
