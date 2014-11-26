/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js sbt plugin        **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013, LAMP/EPFL        **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */


package scala.scalajs.sbtplugin

import sbt._

import scala.scalajs.tools.sem.Semantics
import scala.scalajs.tools.classpath._
import scala.scalajs.tools.io.VirtualJSFile
import scala.scalajs.tools.env.{JSEnv, JSConsole}
import scala.scalajs.tools.optimizer.ScalaJSOptimizer

import scala.scalajs.ir.ScalaJSVersions

import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.env.phantomjs.PhantomJSEnv

object ScalaJSPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin

  object autoImport extends impl.DependencyBuilders {
    import KeyRanks._

    // Some constants
    val scalaJSVersion = ScalaJSVersions.current
    val scalaJSIsSnapshotVersion = ScalaJSVersions.currentIsSnapshot
    val scalaJSBinaryVersion = ScalaJSCrossVersion.currentBinaryVersion

    // Stage values
    val PreLinkStage = Stage.PreLink
    val FastOptStage = Stage.FastOpt
    val FullOptStage = Stage.FullOpt

    // Factory methods for JSEnvs

    /**
     *  Creates a [[Def.Initialize]] for a NodeJSEnv. Use this to explicitly
     *  specify in your build that you would like to run with Node.js:
     *
     *  {{{
     *  postLinkJSEnv := NodeJSEnv().value
     *  }}}
     *
     *  Note that the resulting [[Setting]] is not scoped at all, but must be
     *  scoped in a project that has the ScalaJSPlugin enabled to work properly.
     *  Therefore, either put the upper line in your project settings (common
     *  case) or scope it manually, using [[Project.inScope]].
     */
    def NodeJSEnv(
        executable: String = "node",
        args: Seq[String] = Seq.empty,
        env: Map[String, String] = Map.empty
    ): Def.Initialize[Task[NodeJSEnv]] = Def.task {
      new NodeJSEnv(executable, args, env)
    }

    /**
     *  Creates a [[Def.Initialize]] for a PhantomJSEnv. Use this to explicitly
     *  specify in your build that you would like to run with PhantomJS:
     *
     *  {{{
     *  postLinkJSEnv := PhantomJSEnv().value
     *  }}}
     *
     *  Note that the resulting [[Setting]] is not scoped at all, but must be
     *  scoped in a project that has the ScalaJSPlugin enabled to work properly.
     *  Therefore, either put the upper line in your project settings (common
     *  case) or scope it manually, using [[Project.inScope]].
     */
    def PhantomJSEnv(
        executable: String = "phantomjs",
        args: Seq[String] = Seq.empty,
        env: Map[String, String] = Map.empty,
        autoExit: Boolean = true
    ): Def.Initialize[Task[PhantomJSEnv]] = Def.task {
      val loader = scalaJSPhantomJSClassLoader.value
      new PhantomJSEnv(executable, args, env, autoExit, loader)
    }

    // All our public-facing keys

    val fastOptJS = TaskKey[Attributed[File]]("fastOptJS",
        "Quickly link all compiled JavaScript into a single file", APlusTask)
    val fullOptJS = TaskKey[Attributed[File]]("fullOptJS",
        "Link all compiled JavaScript into a single file and fully optimize", APlusTask)

    val scalaJSStage = SettingKey[Stage]("scalaJSStage",
        "The optimization stage at which run and test are executed", APlusSetting)

    val packageScalaJSLauncher = TaskKey[Attributed[File]]("packageScalaJSLauncher",
        "Writes the persistent launcher file. Fails if the mainClass is ambigous", CTask)

    val packageJSDependencies = TaskKey[File]("packageJSDependencies",
        "Packages all dependencies of the preLink classpath in a single file. " +
        "Set skip in packageJSDependencies := false to run automatically", AMinusTask)

    val jsDependencyManifest = TaskKey[File]("jsDependencyManifest",
        "Writes the JS_DEPENDENCIES file.", DTask)

    val scalaJSPreLinkClasspath = TaskKey[IRClasspath]("scalaJSPreLinkClasspath",
        "Completely resolved classpath just after compilation", DTask)

    val scalaJSExecClasspath = TaskKey[CompleteClasspath]("scalaJSExecClasspath",
        "The classpath used for running and testing", DTask)

    val scalaJSLauncher = TaskKey[Attributed[VirtualJSFile]]("scalaJSLauncher",
        "Code used to run. (Attributed with used class name)", DTask)

    val scalaJSConsole = TaskKey[JSConsole]("scalaJSConsole",
        "The JS console used by the Scala.js runner/tester", DTask)

    val preLinkJSEnv = TaskKey[JSEnv]("preLinkJSEnv",
        "The jsEnv used to execute before linking (packaging / optimizing) Scala.js files", BSetting)
    val postLinkJSEnv = TaskKey[JSEnv]("postLinkJSEnv",
        "The jsEnv used to execute after linking (packaging / optimizing) Scala.js files", AMinusSetting)

    val jsEnv = TaskKey[JSEnv]("jsEnv",
        "A JVM-like environment where Scala.js files can be run and tested", DTask)

    val requiresDOM = SettingKey[Boolean]("requiresDOM",
        "Whether this projects needs the DOM. Overrides anything inherited through dependencies.", AMinusSetting)

    val scalaJSTestFramework = SettingKey[String]("scalaJSTestFramework",
        "The Scala.js class that is used as a test framework, for example a class that wraps Jasmine", ASetting)

    val relativeSourceMaps = SettingKey[Boolean]("relativeSourceMaps",
        "Make the referenced paths on source maps relative to target path", BPlusSetting)

    val emitSourceMaps = SettingKey[Boolean]("emitSourceMaps",
        "Whether package and optimize stages should emit source maps at all", BPlusSetting)

    val jsDependencies = SettingKey[Seq[AbstractJSDep]]("jsDependencies",
        "JavaScript libraries this project depends upon. Also used to depend on the DOM.", APlusSetting)

    val scalaJSSemantics = SettingKey[Semantics]("scalaJSSemantics",
        "Configurable semantics of Scala.js.", BPlusSetting)

    val jsDependencyFilter = SettingKey[PartialClasspath.DependencyFilter]("jsDependencyFilter",
        "The filter applied to the raw JavaScript dependencies before execution", CSetting)

    val checkScalaJSSemantics = SettingKey[Boolean]("checkScalaJSSemantics",
        "Whether to check that the current semantics meet compliance " +
        "requirements of dependencies.", CSetting)

    val persistLauncher = SettingKey[Boolean]("persistLauncher",
        "Tell optimize/package tasks to write the laucher file to disk. " +
        "If this is set, your project may only have a single mainClass or you must explicitly set it", AMinusSetting)

    val scalaJSOptimizerOptions = SettingKey[OptimizerOptions]("scalaJSOptimizerOptions",
        "All kinds of options for the Scala.js optimizer stages", DSetting)

    /** Class loader for PhantomJSEnv. Used to load jetty8. */
    val scalaJSPhantomJSClassLoader = TaskKey[ClassLoader]("scalaJSPhantomJSClassLoader",
        "Private class loader to load jetty8 without polluting classpath. Only use this " +
        "as the `jettyClassLoader` argument of the PhantomJSEnv",
        KeyRanks.Invisible)
  }

  import autoImport._
  import ScalaJSPluginInternal._

  override def globalSettings: Seq[Setting[_]] = {
    super.globalSettings ++ Seq(
        scalaJSStage := Stage.PreLink
    )
  }

  override def projectSettings: Seq[Setting[_]] = (
      scalaJSAbstractSettings ++
      scalaJSEcosystemSettings
  )
}
