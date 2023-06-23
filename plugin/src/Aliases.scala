package com.carlosedp.aliases

import mill._
import mill.define.ExternalModule
import mill.eval.Evaluator

// import scala.reflect.runtime.{universe => ru}

import scala.collection.mutable.AbstractIterable

import Discover._
import AliasRunner._

trait Aliases {
  type alias = Seq[String]
  def alias(xs: String*) = Seq(xs: _*)
}

// For example, defining custom aliases:
// object MyAliases extends Aliases {
//   def demo  = alias("__.test")
//   def demo2 = Seq("__.compile")
//   def demo3 = Seq("__.compile", "__.test")
// }

object Aliases extends ExternalModule {

  /**
   * List all aliases
   *
   * @param ev
   * @return
   */
  def list(ev: Evaluator) =
    T.command(Console.out.println("Listing aliases..."))

  /**
   * Run an alias
   *
   * @param ev
   * @param alias
   * @return
   */
  def run(ev: Evaluator, alias: String) = T.command {
    Console.out.println(s"Running alias $alias")

    // Get all modules which extends Aliases
    val modules = aliasModules(ev)
    println(modules)

    // Get all aliases from the first module which extends Aliases
    val aliases = modules.head
      .getClass()
      .getMethods()
      .map(_.getName())
      .filterNot(
        Seq(
          "wait",
          "equals",
          "toString",
          "hashCode",
          "getClass",
          "notify",
          "notifyAll",
        ).contains(_)
      )
      .toSeq
    println(aliases)

    // Check if the alias exists and get it's tasks
    if (aliases.contains(alias)) {
      // val tasks = classOf[Aliases]
      //   .getDeclaredMethod(alias)
      //   .invoke(Aliases)
      //   .asInstanceOf[Seq[String]]

      val tasks = Seq("__.test") // for testing
      println(tasks)

      aliasRunner(
        ev,
        tasks.flatMap(x => Seq(x, "+")).flatMap(_.split("\\s+")).init,
      )
    } else {
      Console.err.println("Use './mill run [alias]'.");
      Console.out.println("Available aliases:")
      // aliases.foreach(x =>
      //   Console.out.println(
      //     s"${x._1.padTo(15, ' ')} - Commands: (${x._2.mkString(", ")})"
      //   )
      // )
      sys.exit(1)
    }
  }

  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]

  private def aliasModules(ev: Evaluator) =
    ev.rootModule.millInternal.modules.collect { case m: Aliases => m }

}
