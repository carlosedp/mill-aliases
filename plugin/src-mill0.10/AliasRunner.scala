package com.carlosedp.aliases

import mill._
import mill.define.ExternalModule

import Discover._

private[aliases] object AliasRunner extends ExternalModule {

  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]

  def aliasRunner(
    ev:      eval.Evaluator,
    aliases: Seq[String],
  ) = T.command {
    mill.main.MainModule.evaluateTasks(
      ev,
      aliases,
      mill.define.SelectMode.Separated,
    )(identity)
  }
}
