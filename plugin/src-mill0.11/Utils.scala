package com.carlosedp.aliases

import mill._
import mill.api.Result
import mill.resolve.{Resolve, SelectMode}

private object Utils {
    def taskResolver(ev: eval.Evaluator, tasks: Seq[String]): Result[List[String]] = {
        val resolved = Resolve.Segments.resolve(
            ev.rootModule,
            tasks,
            SelectMode.Multi,
        )

        resolved match {
            case Left(err)                   => Result.Failure(err)
            case Right(resolvedSegmentsList) =>
                val resolvedStrings = resolvedSegmentsList.map(_.render)
                // resolvedStrings.sorted.foreach(Console.out.println) // Print all resolved tasks
                Result.Success(resolvedStrings)
        }
    }
}
