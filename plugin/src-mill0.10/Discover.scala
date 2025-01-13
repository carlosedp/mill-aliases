package com.carlosedp.aliases

import mill.main.EvaluatorScopt

object Discover {
    implicit def millScoptEvaluatorReads[A]: EvaluatorScopt[A] =
        new EvaluatorScopt[A]()
}
