// Define packageless commands for easy access

import com.carlosedp.aliases._
import mainargs.arg
import mill._
import mill.define.ExternalModule
import mill.eval.Evaluator

object Alias extends ExternalModule {
    lazy val millDiscover: mill.define.Discover =
        mill.define.Discover[this.type]

    def run(ev: Evaluator, @arg(positional = true) alias: String) = Task.Command(exclusive = true) {
        AliasesModule.run(ev, alias)
    }
    def list(ev: Evaluator) = Task.Command(exclusive = true) {
        AliasesModule.list(ev)
    }
    def help() = Task.Command(exclusive = true) {
        AliasesModule.help()
    }
}
