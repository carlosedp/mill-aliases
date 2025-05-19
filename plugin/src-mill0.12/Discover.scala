package com.carlosedp.aliases

import mainargs.TokensReader
import mill.eval.Evaluator

object Discover {
    implicit def millEvaluatorTokenReader: TokensReader[Evaluator] =
        mill.main.TokenReaders.millEvaluatorTokenReader
}
