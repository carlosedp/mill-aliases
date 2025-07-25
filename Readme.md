# mill-aliases

![Mill 0.x](https://img.shields.io/maven-central/v/com.carlosedp/mill-aliases_mill0.11_2.13)
![Mill 1.0](https://img.shields.io/maven-central/v/com.carlosedp/mill-aliases_mill1_3)

This is a Scala [Mill](http://mill-build.com/) plugin adding the task alias capability to the build tool. Supports Mill 0.10 and 0.11, 0.12 and 1.0 series.

## Getting Started

To use the plugin on Mill 0.x series, add the following to your `build.sc` / `build.mill`:

```scala
import $ivy.`com.carlosedp::mill-aliases::0.7.0`
import com.carlosedp.aliases._
```

To use the plugin on Mill 1.0 series, add the following to your `build.mill`:

```scala
//| mill-version: 1.0.1
//| mvnDeps:
//| - com.carlosedp::mill-aliases::1.1.0

import com.carlosedp.aliases.*
```

The latest version for series 0.x is 0.7.0. To use the latest functionalities, please use the 1.0 series which is currently at version 1.0.0. There is full functionality parity between version 0.7.0 and 1.0.0. New features will be added to the 1.0 series only.

To define your project aliases, create an object extending the `Aliases` trait containing one method per required alias. Aliases are global to your project (whether single or multi-module) and are defined at the root level of your build file.

Aliases can be single string tasks or a sequence of strings containing multiple tasks pointing to your module's tasks and are defined using the `alias` type in your `build.sc` / `build.mill`, eg:

```scala
object mymodule extends ScalaModule {
  ... // Your module here
}

object MyAliases extends Aliases {
  def fmt         = alias("mill.scalalib.scalafmt/ __.sources")
  def checkfmt    = alias("mill.scalalib.scalafmt/checkFormatAll __.sources")
  def lint        = alias("mill.scalalib.scalafmt/ __.sources", "__.fix")
  def testall     = alias("__.test")

  // Aliases that reference other aliases and/or tasks
  def fmtAndCheck = alias("fmt", "checkfmt")
  def fullPrep    = alias("fmt", "lint", "__.test")
}
```

Aliases can also reference other aliases, allowing you to create complex workflows by composing simpler ones. When an alias task matches the name of another alias, the plugin will automatically execute that alias instead of treating it as a mill task.

If you use Zsh as shell and/or P10k as a theme, check my Zsh Mill completions plugin at <https://github.com/carlosedp/mill-zsh-completions>. It supports getting Mill tasks and aliases.

## Usage

**To show all the defined aliases:**

When listing aliases, tasks that reference other aliases are marked with an arrow (`→`):

```sh
./mill Alias/list
```

Output:

```sh
Available aliases:

┌───────────────┬───────────────┬────────────────────────────────────────────────────────────┐
│ Alias         │ Module        │ Command(s)                                                 │
├───────────────┼───────────────┼────────────────────────────────────────────────────────────┤
│ checkfmt      │ MyAliases     │ mill.scalalib.scalafmt/checkFormatAll __.sources          │
│ fmt           │ MyAliases     │ mill.scalalib.scalafmt/ __.sources                         │
│ fmtAndCheck   │ MyAliases     │ fmt→, checkfmt→                                            │
│ fullPrep      │ MyAliases     │ fmt→, lint→, testall→                                      │
│ lint          │ MyAliases     │ mill.scalalib.scalafmt/ __.sources, __.fix                │
│ testall       │ MyAliases     │ __.test                                                    │
└───────────────┴───────────────┴────────────────────────────────────────────────────────────┘

Legend:
  Colored tasks→ Reference other aliases
  Green names     Alias names
```

**Run an alias:**

```sh
./mill Alias/run testall
```

In this case, the task `__.test` will be run which is executing the test task for all your modules.

```sh
./mill Alias/run fullPrep
```

In this case, the aliases `fmt`, `lint`, and `testall` will be run in sequence, executing the tasks defined in those aliases.


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
