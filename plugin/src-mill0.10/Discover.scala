package com.carlosedp.aliases

import mill._
import mill.main.EvaluatorScopt

trait MillDiscover extends Module {
    lazy val millDiscover: mill.define.Discover[this.type] =
        mill.define.Discover[this.type]
}

object Discover {
    implicit def millScoptEvaluatorReads[A]: EvaluatorScopt[A] =
        new EvaluatorScopt[A]()
}
