package com.carlosedp.aliases

import mill._
import mill.api.Result
import mill.api.Result.{Aborted, Exception, Failure, Skipped, Success}
import mill.define.ExternalModule
import mill.util.Watched

private[aliases] object AliasRunner extends ExternalModule with MillDiscover {

    def aliasRunner(ev: eval.Evaluator, aliases: Seq[String]): Result[Watched[Unit]] =
        mill.main.MainModule.evaluateTasks(
            ev,
            aliases,
            mill.define.SelectMode.Separated,
        )(identity) match {
            case Aborted                          => Failure("Aborted")
            case Exception(throwable, outerStack) => Exception(throwable, outerStack)
            case Failure(msg, value)              => Failure(msg, value)
            case Skipped                          => Skipped
            case Success(value)                   => Success(value)
        }
}
