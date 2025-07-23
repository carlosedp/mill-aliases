// Define packageless commands for easy access

import com.carlosedp.aliases.AliasesModule
import mainargs.arg
import mill.api.{Discover, Evaluator, ExternalModule}
import mill.{Task, given}

object Alias extends ExternalModule:
  lazy val millDiscover = Discover[this.type]

  def run(ev: Evaluator, @arg(positional = true) alias: String) =
    Task.Command(exclusive = true) {
      AliasesModule.run(ev, alias)
    }

  def list(ev: Evaluator) = Task.Command(exclusive = true) {
    AliasesModule.list(ev)
  }

  def help() = Task.Command(exclusive = true) {
    AliasesModule.help()
  }
end Alias
