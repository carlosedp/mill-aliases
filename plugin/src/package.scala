import mill.define.ExternalModule
import mill.eval.Evaluator

import com.carlosedp.aliases._
import com.carlosedp.aliases.Discover._

object Alias extends ExternalModule {
  def run(ev:  Evaluator, alias: String) = Aliases.run(ev, alias)
  def list(ev: Evaluator) = Aliases.list(ev)

  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]

}
