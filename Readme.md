# mill-aliases

![Maven Central](https://img.shields.io/maven-central/v/com.carlosedp/mill-aliases_mill0.11_2.13)

This is a Scala [Mill](http://mill-build.com/) plugin adding the task alias capability to the build tool. Supports Mill 0.10 and 0.11 series.

## Getting Started

First import the plugin into your `build.sc` / `build.mill` using the latest published version, by adding:

```scala
import $ivy.`com.carlosedp::mill-aliases::0.6.0`  //ReleaseVerMill
import com.carlosedp.aliases._
```

To define your project aliases, create an object extending the `Aliases` trait containing one method per required alias. Aliases are global to your project (whether single or multi-module) and are defined at the root level of your build file.

Aliases can be single string tasks or a sequence of strings containing multiple tasks pointing to your module's tasks and are defined using the `alias` type in your `build.sc` / `build.mill`, eg:

```scala
import mill._, scalalib._

object mymodule extends ScalaModule {
  ... // Your module here
}

object MyAliases extends Aliases {
  def testall     = alias("__.test")
  def compileall  = alias("__.compile")
  def comptestall = alias("__.compile", "__.test")
}
```

If you use Zsh as shell and/or P10k as a theme, check my Zsh Mill completions plugin at <https://github.com/carlosedp/mill-zsh-completions>. It supports getting Mill tasks and aliases.

## Usage

**To show all the defined aliases:**

```sh
./mill Alias/list
```

Which will show (eg.):

```sh
Use './mill run [alias]'.
Available aliases:
┌─────────────────┬─────────────────┬───────────────────────────────────────────────────────────────────────────────────
| Alias           | Module          | Command(s)
├─────────────────┼─────────────────┼───────────────────────────────────────────────────────────────────────────────────
| compall         | MyAliases       | (__.compile)
| comptestall     | MyAliases       | (__.compile, __.test)
| testall         | MyAliases       | (__.test)
| deps            | MyAliases       | (mill.scalalib.Dependency/showUpdates)
└─────────────────┴─────────────────┴───────────────────────────────────────────────────────────────────────────────────
```

**Run an alias:**

```sh
./mill Alias/run testall
```

In this case, the task `__.test` will be run which is executing the test task for all your modules.

```sh
./mill Alias/run comptestall
```

In this case, the task `__.compile` will be run first followed by `__.test` which will first compile all sources from all modules and then test all modules.

**To show the help:**

```sh
./mill Alias/help
```

Which displays:

```sh
--------------------
Mill Aliases Plugin
--------------------
The plugin allows you to define aliases for mill tasks.
The aliases are defined in an object extending `Aliases` in the build.sc file at the root level in the following format:

  object MyAliases extends Aliases {
    def testall     = alias("__.test")
    def compileall  = alias("__.compile")
    def testcompall = alias("__.compile", "__.test")
  }

Aliases can be defines for one or multiple tasks which will be run in sequence.
The aliases can be run with './mill Alias/run [alias]'.
To list all aliases: './mill Alias/list'
```

Aliases can also be defined on separated objects extending `Aliases` to help with build organization. If a name conflict happens for aliases on different modules, the list command will show all the aliases and it's module but when run, only the alias on the first module defined will be executed.
