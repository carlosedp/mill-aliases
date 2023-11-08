package com.carlosedp.aliases

import mill._
import mill.api.Result
import mill.define.SelectMode

private object Utils {
    def taskResolver(ev: eval.Evaluator, tasks: Seq[String]): Result[List[String]] = {
        val resolved: Either[String, List[String]] =
            main.RunScript.resolveTasks(
                main.ResolveMetadata,
                ev,
                tasks,
                SelectMode.Multi,
            )

        resolved match {
            case Left(err) => Result.Failure(err)
            case Right(rs) =>
                // rs.sorted.foreach(Console.out.println) // Print all resolved tasks
                Result.Success(rs)
        }
    }
}
