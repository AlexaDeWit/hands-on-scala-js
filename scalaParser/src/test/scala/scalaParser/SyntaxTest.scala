package scalaParser

import org.parboiled2.ParseError
import utest._
import utest.framework.Test
import utest.util.Tree

import scala.util.{Failure, Success}

object SyntaxTest extends TestSuite{
  def checkNeg[T](input: String) = {
    println("Checking...")
    new ScalaSyntax(input).CompilationUnit.run() match{
      case Failure(f: ParseError) => () // yay
      case Success(parsed) => assert(parsed.length != input.length)
    }
  }
  def check[T](input: String) = {
    println("Checking...")
    new ScalaSyntax(input).CompilationUnit.run() match{
      case Failure(f: ParseError) =>
        println(f.position)
        println(f.formatExpectedAsString)
        println(f.formatTraces)
        throw new Exception(f.position + "\t" + f.formatTraces)
      case Success(parsed) =>
        if(parsed != input)

          throw new Exception(
            "Parsing Failed at " + parsed.length + "\n" + input.drop(parsed.length).take(50)
          )
    }
  }
  println("running")
  def tests = TestSuite{
    'unit {
      'pos {
        * - check(
          "package torimatomeru"

        )
        * - check(
          """package torimatomeru
            |
            |package lols
          """.stripMargin
        )
        * - check(
          """package torimatomeru
            |import a
            |import b
          """.stripMargin
        )
        * - check(
          """
            |package torimatomeru
            |
            |import org.parboiled2.ParseError
            |import utest._
            |import utest.framework.Test
            |import utest.util.Tree
            |
            |import scala.util.{Failure, Success}
            |
            |object SyntaxTest extends TestSuite
          """.stripMargin
        )
        * - check(
          """
            |object SyntaxTest extends TestSuite{
            |  def check[T](input: String) = {
            |
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """
            |object SyntaxTest{
            |  a()
            |  throw 1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object SyntaxTest extends TestSuite{
            |  {
            |        println
            |        throw 1
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """package scalatex
            |
            |
            |import org.parboiled2._
            |import torimatomeru.ScalaSyntax
            |
            |import scalatex.stages.{Trim, Parser, Ast}
            |import scalatex.stages.Ast.Block.{IfElse, For, Text}
            |import Ast.Chain.Args
            |
            |object ParserTests extends utest.TestSuite{
            |  import Ast._
            |  import utest._
            |  def check[T](input: String, parse: Parser => scala.util.Try[T], expected: T) = {
            |    val parsed = parse(new Parser(input)).get
            |    assert(parsed == expected)
            |  }
            |  def tests = TestSuite{}
            |}
          """.stripMargin
        )
        * - check(
          """
            |object Moo{
            |  a
            |  .b
            |
            |  c
            |}
          """.stripMargin
        )
        * - check(
          """
            |object Moo{
            | filename
            |        .asInstanceOf[Literal]
            |10
            |}
          """.stripMargin
        )
        * - check(
          """
            |object Cow{
            |  ().mkString
            |
            |  1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            | private[this] val applyMacroFull = 1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            | private[this] def applyMacroFull(c: Context)
            |                      (expr: c.Expr[String],
            |                       runtimeErrors: Boolean,
            |                       debug: Boolean)
            |                      : c.Expr[Frag] = {
            |                      }
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |  class DebugFailure extends Exception
            |
            |  1
            |}
          """.stripMargin
        )
        * - check(
          """
            |package torimatomeru
            |
            |package syntax
            |
            |import org.parboiled2._
            |
          """.stripMargin
        )
        * - check(
          """
            |object Foo{
            |  0 match {
            |    case A | B => 0
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """
          |object Compiler{
          |
          |  def apply = {
          |    def rec = t match {
          |      case 0 => 0
          |    }
          |
          |    rec(tree)
          |  }
          |}
          |
        """.
            stripMargin
        )
        * - check(
          """
            |object O {
            |    A(A(A(A(A(A(A(A())))))))
            |}
            |
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |   A(A(A(A(A(A(A(A(A(A(A(A(A(A(A(A())))))))))))))))
            |}
          """.stripMargin
        )
        * - check(
          """
            |object L{
            |  a.b = c
            |  a().b = c
            |}
          """.stripMargin
        )
        * - check(
          """
            |object L{
            |  a b c
            |  d = 1
            |}
          """.stripMargin
        )

        * - check(
          """/*                     __                                               *\
            |**     ________ ___   / /  ___      __ ____  Scala.js CLI               **
            |**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013-2014, LAMP/EPFL   **
            |**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
            |** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
            |**                          |/____/                                     **
            |\*                                                                      */
            |
            |package scala.scalajs.cli
            |
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |  for {
            |      a  <- b
            |      c <- d
            |  } {
            |    1
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |  val jarFile =
            |      try { 1 }
            |      catch { case _: F => G }
            |}
          """.stripMargin
        )
        * - check(
          """
            |object F{
            |  func{ case _: F => fail }
            |}
          """.stripMargin
        )
        * - check(
          """
            |object Foo{
            |    val a = d // g
            |    val b = e // h
            |    val c = f
            |}
          """.stripMargin
        )
        * - check(
          """
            |object L{
            |  x match{
            |    case y.Y(z) => z
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """object K{
            |  val a: B {
            |    val c: D
            |  }
            |
            |  1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object LOLS{
            |    def run() {}
            |
            |    def apply() {}
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |  a =:= b.c
            |}
          """.stripMargin
        )
        * - check(
          """
            |object K{
            |  a(
            |    1: _*
            |  )
            |}
          """.stripMargin
        )
        * - check(
          """
            |object P{
            |      tree match {
            |        case stats :+ expr  => 1
            |      }
            |}
          """.stripMargin
        )
        * - check(
          """
            |object K{
            |  val trueA = 1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object K{
            |  val nullo :: cow = 1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object K{
            |  val omg_+ = 1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object K{
            |  val + = 1
            |  var * = 2
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |  c match {
            |    case b_  => 1
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """
            |trait Basic {
            |  b match {
            |    case C => true; case _ => false
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """trait Basic {
            |  !a.b
            |}
          """.stripMargin
        )
        * - check(
          """
            |class Parser {
            |  {() => }
            |}
            |
          """.stripMargin
        )
        * - check(
          """
            |
            |
            |
            |package omg
            |;
            |
            |;
            |
            |;
            |class Parser
            |;
            |
            |;
            |
            |;
          """.stripMargin
        )
        * - check(
          """
            |
            |object GenJSCode {
            |  code: @switch
            |}
          """.stripMargin
        )
        * - check(
          """object B {
            |  { a: L => }
            |}
          """.stripMargin
        )
        * - check(
          """object O{
            |  {
            |    val index = 0
            |    i: Int => 10
            |    0
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """object GenJSCode{
            |  val g: G.this.g.type
            |}
            |
          """.stripMargin
        )
        * - check(
          """object K{
            |  class RTTypeTest
            |  private object O
            |}
          """.stripMargin
        )
        * - check(
          """object O{
            |  if (eqeq &&
            |
            |    false)  1
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |  for(
            |    x <- Nil map
            |
            |  (x => x)
            |  ) yield x
            |}
          """.stripMargin
        )
        * - check(
          """
            |object O{
            |  for{
            |    x <- Nil
            |    if
            |
            |    1 == 2
            |  } yield x
            |}
          """.stripMargin
        )
        * - check(
          """
            |object ScopedVar {
            |  def withScopedVars(ass: Seq[_]) = 1
            |}
            |
          """.stripMargin
        )
        * - check(
          """
            |abstract class JSASTTest extends DirectTest {
            |  def show: this.type = ()
            |}
            |
          """.stripMargin
        )
        * - check(
          """object Traversers {
            |  {
            |        1
            |        cases foreach nil
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """object Utils {
            |  "\\"
            |}
            |
          """.stripMargin
        )
        * - check(
          """object F{
            |  this eq that.asInstanceOf[AnyRef]
            |}
          """.stripMargin
        )
        * - check(
          """class C{
            |  0x00 <= 2 && 1
            |}
            |
          """.stripMargin
        )
        * - check(
          """class Runtime private
          """.stripMargin
        )
        * - check(
          """
            |object System {
            |  def a[@b T[@b V]] = 1
            |}
            |
          """.stripMargin
        )
        * - check(
          """object U{
            |  private val _fragment = fld(Fragment)
            |  _fld = null
            |}
          """.stripMargin
        )
        * - check(
          """class Array{
            |  def length_= = 1
            |}
          """.stripMargin
        )
        * - check(
          """object K{
            |  def newBuilder =
            |    new B
            |
            |  @inline def a = 1
            |}
          """.stripMargin
        )
        * - check(
          """trait Function12[-T1, +R]
          """.stripMargin
        )
        * - check(
          """@a // Don't do this at home!
            |trait B
          """.stripMargin
        )
        * - check(
          """object T{
            |  type B = { def F: S }
            |}
            |
          """.stripMargin
        )
        * - check(
          """
            |object ScalaJSBuild{
            |      (
            |        1 / 2
            |          / 3
            |      )
            |}
            |
          """.stripMargin
        )
        * - check(
          """trait Writer{
            | '\f'
            |}
          """.stripMargin
        )
        * - check(
          """object CyclicDependencyException {
            |    def str(info: ResolutionInfo) =
            |      s"${info.resourceName} from: ${info.origins.mkString(", ")}"
            |}
          """.stripMargin
        )
        * - check(
          """object OptimizerCore {
            |  tpe match {
            |    case NothingType | _:RecordType=> 1
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """class A{
            |  1
            |  () => 1
            |}
          """.stripMargin
        )
        * - check(
          """trait ReactorCanReply {
            |  _: InternalReplyReactor =>
            |}
          """.stripMargin
        )

        * - check(
          """object G{
            |  def isBefore(pd: SubComponent) = settings.stopBefore
            |  phaseDescriptors sliding 2 collectFirst ()
            |}
          """.stripMargin
        )
        * - check(
          """class SymbolLoaders {
            |  type T = ClassPath[AbstractFile]#ClassRep
            |}
          """.stripMargin
        )
        * - check(
          """trait ContextErrors {
            |    def isUnaffiliatedExpr = expanded.isInstanceOf[scala.reflect.api.Exprs#Expr[_]]
            |}
          """.stripMargin
        )
        * - check(
          """trait Typers{
            |  s"nested ${ if (1) "trait" else "class" }"
            |}
          """.stripMargin
        )
        * - check(
          """trait ReflectSetup { this: Global =>
            |  phase = 1
            |}
          """.stripMargin
        )
        * - check(
          """trait Predef {
            |  @x
            |  // a
            |  type T
            |}
          """.stripMargin
        )
        * - check(
          """
            object StringContext {

              s"${
                require(index >= 0 && index < str.length)
                val ok = "[\b, \t, \n, \f, \r, \\, \", \']"
                if (index == str.length - 1) "at terminal" else s"'\\${str(index + 1)}' not one of $ok at"
              }"

            }
          """.stripMargin
        )
        * - check(
          """trait Growable {
            |    +=
            |}
          """.stripMargin
        )
        * - check(
          """package immutable {
            |  object O
            |}
          """.stripMargin
        )
        * - check(
          """import java.util.concurrent.TimeUnit.{ NANOSECONDS => NANOS, MILLISECONDS ⇒ MILLIS }
          """.stripMargin
        )
        * - check(
          """class FunFinder{
            |  val targetName = s"$name${ if (isModule) "$" else "" }"
            |}
          """.stripMargin
        )
        * - check(
          """class AkkaException{
            |  for (i ← 0 until trace.length)
            |    ()
            |}
          """.stripMargin
        )
        * - check(
          """class FiniteDuration{
            |  1000.
            |}
          """.stripMargin
        )
        * - check(
          """object Test4 {
            |    type T = F @field
            |    @BeanProperty val x = 1
            |}
          """.stripMargin
        )
        * - check(
          """package `dmacro` {
            |}
          """.stripMargin
        )
        * - check(
          """class A {
            |  def fn1 = List apply 1
            |  def fn2 = List apply[Int] 2
            |}
          """.stripMargin
        )
        * - check(
          """class C {
            |  def this(x: Int) = {
            |    this();
            |    class D;
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """trait B[T] {
            |  def f1(a: T): Unit { }
            |}
          """.stripMargin
        )
        * - check(
          """object test {
            |  case object Int16 extends SampleFormat1
            |  (1) match {
            |    case _   => 1
            |  }
            |}
          """.stripMargin
        )
        * - check(
          """object A {
            |  def x {
            |    implicit lazy val e: Int = 0
            |  }
            |}
          """.stripMargin
        )
      }
      'neg{
        * - checkNeg(
          """
            |object O{
            |  for{
            |    x <- Nil map
            |
            |  (x => x)
            |  } yield x
            |}
          """.stripMargin
        )
        * - checkNeg(
          """object O{
            |  for{
            |    x <- Nil
            |    if 1 ==
            |
            |    2
            |  } yield x
            |}
          """.stripMargin
        )
        * - checkNeg(
          """object O{
            |  for{
            |    x <- Nil
            |    _ = 1 ==
            |
            |    2
            |  } yield x
            |}
          """.stripMargin
        )
        * - checkNeg(
          """
            |object System {
            |  def a[@b T[V @b]] = 1
            |}
            |
          """.stripMargin
        )

      }
    }
    def checkFile(path: String) = check(io.Source.fromFile(path).mkString)
    'file{

      * - checkFile("scalaParser/src/test/resources/test.scala")
      * - checkFile("scalaParser/src/main/scala/scalaParser/syntax/Basic.scala")
      * - checkFile("scalaParser/src/main/scala/scalaParser/syntax/Identifiers.scala")
      * - checkFile("scalaParser/src/main/scala/scalaParser/syntax/Literals.scala")
      * - checkFile("scalaParser/src/main/scala/scalaParser/ScalaSyntax.scala")

      * - checkFile("scalaParser/src/test/scala/scalaParser/SyntaxTest.scala")


      * - checkFile("scalatexApi/src/main/scala/scalatex/stages/Compiler.scala")
      * - checkFile("scalatexApi/src/main/scala/scalatex/stages/Parser.scala")
      * - checkFile("scalatexApi/src/main/scala/scalatex/stages/Trim.scala")
      * - checkFile("scalatexApi/src/main/scala/scalatex/package.scala")

      * - checkFile("scalatexApi/src/test/scala/scalatex/ParserTests.scala")
      * - checkFile("scalatexApi/src/test/scala/scalatex/BasicTests.scala")
      * - checkFile("scalatexApi/src/test/scala/scalatex/ErrorTests.scala")
      * - checkFile("scalatexApi/src/test/scala/scalatex/TestUtil.scala")

      * - checkFile("scalatexPlugin/src/main/scala/scalatex/ScalaTexPlugin.scala")
    }

    'omg{
//      val root = new java.io.File("book/target/clones/scala-js/")
      val root = new java.io.File("../scala")
      def listFiles(s: java.io.File): Iterator[String] = {
        val (dirs, files) = s.listFiles().toIterator.partition(_.isDirectory)
        files.map(_.getPath) ++ dirs.flatMap(listFiles)
      }
      // Things that we won't bother parsing, mainly because they use XML literals
      val blacklist = Seq(
        "dbuild-meta-json-gen.scala",
        "genprod.scala",
        "doc/html/HtmlPage.scala",
        "scala/src/scaladoc/scala/tools/nsc/doc/html",
        "jvm/interpreter.scala",
        "disabled", // don't bother parsing disabled tests
        "neg" // or neg tests
      )
      for{
        f <- listFiles(root)
        if f.endsWith(".scala")
        if !blacklist.exists(f.contains)
      }{
        println("CHECKING " + f)
        checkFile(f)
      }
    }
  }
}