// Define packageless commands for easy access

import com.carlosedp.aliases.Discover._
import com.carlosedp.aliases._
import mill._
import mill.define.ExternalModule
import mill.eval.Evaluator

object Alias extends ExternalModule {
    def run(ev: Evaluator, alias: String) = T.command {
        AliasesModule.run(ev, alias)
    }
    def list(ev: Evaluator) = T.command(AliasesModule.list(ev))
    def help()              = T.command(AliasesModule.help())

    lazy val millDiscover: mill.define.Discover[this.type] =
        mill.define.Discover[this.type]
}
