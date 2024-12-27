package com.carlosedp.aliases

import mainargs.TokensReader
import mill.eval.Evaluator
import mill._

trait MillDiscover extends Module {
    lazy val millDiscover: mill.define.Discover =
        mill.define.Discover[this.type]
}

object Discover {
    implicit def millEvaluatorTokenReader: TokensReader[Evaluator] =
        mill.main.TokenReaders.millEvaluatorTokenReader
}
