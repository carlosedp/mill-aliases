# mill-aliases

**This plugin is still a work-in-progress and does not work as expected. The idea is to collect feedback and adjust according to needs.**

This is a Scala [Mill](http://mill-build.com/) plugin adding the task alias capability to the build tool.

## Getting Started

First import the plugin into your `build.sc` using the latest published version, by adding:

```scala
import $ivy.`com.carlosedp::mill-aliases::0.0.0-SNAPSHOT`
import com.carlosedp.aliases._
```

To define your project aliases, create an object extending the `Aliases` trait containing one method per required alias. Aliases are global to your project (whether single or multi-module) and are defined at the root level of your `build.sc` file.

Aliases can be single string tasks or a sequence of strings containing multiple tasks pointing to your module's tasks and are defined using the `alias` type in your `build.sc`, eg:

```scala
import mill._, scalalib._

object module1 extends ScalaModule {
  ... // Your module here
}

object MyAliases extends Aliases {
  def testall     = alias("__.test")
  def compileall  = alias("__.compile")
  def comptestall = alias("__.compile", "__.test")
}
```

## Usage

To show all the defined aliases:

```sh
./mill Alias/list
```

> *Command output is TBD, but something like:*

```sh
Use './mill Alias/run [alias]'.
Available aliases:
testall         - Commands: (__.test)
deps            - Commands: (mill.scalalib.Dependency/showUpdates)
fmt             - Commands: (mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources)
lint            - Commands: (mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources, __.fix)
```

Run an alias:

```sh
./mill Alias/run testall
```

In this case, the task `__.test` will be run which is executing the test task for all your modules.

```sh
./mill Alias/run comptestall
```

In this case, the task `__.compile` will be run first followed by `__.test` which will first compile all sources from all modules and then test all modules.
