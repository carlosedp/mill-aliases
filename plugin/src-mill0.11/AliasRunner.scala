package com.carlosedp.aliases

import mill._
import mill.define.ExternalModule

private[aliases] object AliasRunner extends ExternalModule {

  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]

  def aliasRunner(
    ev:      eval.Evaluator,
    aliases: Seq[String],
  ) = T.command {
    Console.out.println("Running aliases in mill 0.11...")
    Console.out.println(aliases)
    println("Running aliases...")
    println(aliases)

    mill.main.RunScript.evaluateTasksNamed(
      ev.withFailFast(false),
      aliases,
      mill.resolve.SelectMode.Separated,
    )
    ()
  }
}
