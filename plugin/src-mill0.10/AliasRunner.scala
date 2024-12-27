package com.carlosedp.aliases

import mill._
import mill.api.Result
import mill.api.Result.{Aborted, Failure, Skipped, Success}
import mill.define.ExternalModule
import mill.util.Watched

private[aliases] object AliasRunner extends ExternalModule {

    lazy val millDiscover: mill.define.Discover[this.type] =
        mill.define.Discover[this.type]

    def aliasRunner(ev: eval.Evaluator, aliases: Seq[String]): Result[Watched[Unit]] =
        mill.main.MainModule.evaluateTasks(
            ev,
            aliases,
            mill.define.SelectMode.Separated,
        )(identity) match {
            case Aborted                                 => Result.Failure("Aborted")
            case Result.Exception(throwable, outerStack) => Result.Exception(throwable, outerStack)
            case Failure(msg, value)                     => Result.Failure(msg, value)
            case Skipped                                 => Result.Skipped
            case Success(value)                          => Result.Success(value)
        }
}
