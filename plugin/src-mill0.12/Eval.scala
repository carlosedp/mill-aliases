package com.carlosedp.aliases

import mill.eval.Evaluator

private[aliases] object Eval {
    def evalOrThrow(ev: Evaluator) = ev.evalOrThrow()
}
