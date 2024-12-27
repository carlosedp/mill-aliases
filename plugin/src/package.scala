// Define packageless commands for easy access

import com.carlosedp.aliases._
import mainargs.arg
import mill._
import mill.define.ExternalModule
import mill.eval.Evaluator

object Alias extends ExternalModule with MillDiscover {
    def run(ev: Evaluator, @arg(positional = true) alias: String) = T.command {
        AliasesModule.run(ev, alias)
    }
    def list(ev: Evaluator) = T.command(AliasesModule.list(ev))
    def help()              = T.command(AliasesModule.help())
}
