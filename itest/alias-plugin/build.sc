// mill plugins under test
import $file.plugins
import mill._
import mill.define.Command
import mill.eval.Evaluator

import com.carlosedp.aliases._

object MyAliases extends Aliases {
  def testall     = alias("__.test")
  def compileall  = alias("__.compile")
  def testcompall = alias("__.compile", "__.test")
}

// -----------------------------------------------------------------------------
// Test cases
// -----------------------------------------------------------------------------
// - Check if plugin prints help message
// - Check if plugin lists existing aliases
// - Check alias with one task
// - Check alias with two tasks
// - Check alias with one task having arguments
// - Check if invalid (non-existing) alias returns error
// - Check alias with invalid task returns error
// - Check alias with a task that returns error, propagates the error

def verify(ev: Evaluator): Command[Unit] = T.command {
  locally {
    val output = AliasesModule.help()
    // Console.out.println(output)
    assert(output.contains("Mill Aliases Plugin"), "Help message should contain plugin header")

  }

  locally {
    val modules = AliasesModule.list(ev)
    // Console.out.println(modules)
    assert(modules.contains("testall"), "Alias testall should be listed")
    assert(modules.contains("__.test"), "Alias task __.test should be listed")
    assert(modules.contains("compileall"), "Alias compileall should be listed")
    assert(modules.contains("__.compile"), "Alias task __.compile should be listed")

  }

}
