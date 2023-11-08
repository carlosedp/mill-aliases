package com.carlosedp.aliases

import mill._
import mill.api.Result
import mill.define.ExternalModule

private[aliases] object AliasRunner extends ExternalModule {

    lazy val millDiscover: mill.define.Discover[this.type] =
        mill.define.Discover[this.type]

    def aliasRunner(ev: eval.Evaluator, aliases: Seq[String]) =
        mill.main.RunScript.evaluateTasksNamed(
            ev.withFailFast(false),
            aliases,
            mill.resolve.SelectMode.Separated,
        ) match {
            case Left(value) => Result.Failure(value)
            case Right(value) => value._2 match {
                    case Left(value2)  => Result.Failure(value2)
                    case Right(value2) => Result.Success(value2)
                }
        }
}
