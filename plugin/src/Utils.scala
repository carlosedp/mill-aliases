package mill.aliasesUtil

import com.carlosedp.aliases.Aliases
import mill.*
import mill.api.daemon.Result.{Failure, Success}
import mill.api.{Evaluator, Result, SelectMode}

object Utils:
  def taskResolver(ev: Evaluator, tasks: Seq[String]): Result[List[String]] =
    val resolved = ev.resolveTasks(
      tasks,
      SelectMode.Multi,
    )
    resolved match
      case Failure(error) => Result.Failure(error)
      case Success(value) =>
        Result.Success(value.map(_.toString).toList)

  /**
   * Get all modules that extend [[Aliases]] in the project
   *
   * @param ev
   *   Evaluator
   * @return
   *   A `Seq` of [[Aliases]] modules
   */
  def aliasModules(ev: Evaluator): Seq[Aliases] =
    // Get all modules that extend Aliases
    ev.rootModule.moduleInternal.modules.collect { case m: Aliases => m }
end Utils
