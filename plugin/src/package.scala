// Define packageless commands for easy access

import mill.define.ExternalModule
import mill.eval.Evaluator

import com.carlosedp.aliases._
import com.carlosedp.aliases.Discover._

object Alias extends ExternalModule {
  def run(ev:  Evaluator, alias: String) = AliasesModule.run(ev, alias)
  def list(ev: Evaluator) = AliasesModule.list(ev)
  def help() = AliasesModule.help()

  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]

}
