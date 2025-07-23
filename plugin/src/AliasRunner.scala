package com.carlosedp.aliases

import mill.api.daemon.Result.{Failure, Success}
import mill.api.{Evaluator, Result, SelectMode}

private[aliases] object AliasRunner:
  def aliasRunner(ev: Evaluator, aliases: Seq[String]) =
    ev.evaluate(
      aliases,
      SelectMode.Separated,
    ) match
      case Failure(error) => Result.Failure(error)
      case Success(value) => value._2 match
          case Failure(error)  => Result.Failure(error)
          case Success(result) => Result.Success(result)
