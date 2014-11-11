package scalatex
package stages
import acyclic.file
import org.parboiled2._
import torimatomeru.ScalaSyntax

/**
 * Parses the input text into a roughly-structured AST. This AST
 * is much simpler than the real Scala AST, but serves us well
 * enough until we stuff the code-strings into the real Scala
 * parser later
 */
object Parser extends ((String, Int) => Ast.Block){
  def apply(input: String, offset: Int = 0): Ast.Block = {
    new Parser(input, offset).Body.run().get
  }
}
class Parser(input: ParserInput, indent: Int = 0, offset: Int = 0) extends ScalaSyntax(input) {
  def offsetCursor = offset + cursor
  val txt = input.sliceString(0, input.length)
  val indentTable = txt.split('\n').map{ s =>
    if (s.trim == "") -1
    else s.takeWhile(_ == ' ').length
  }
  val nextIndentTable = (0 until indentTable.length).map { i =>
    val index = indentTable.indexWhere(_ != -1, i + 1)
    if (index == -1) 100000
    else indentTable(index)
  }
  def cursorNextIndent() = {
    nextIndentTable(txt.take(cursor).count(_ == '\n'))
  }

  def TextNot(chars: String) = rule {
    push(offsetCursor) ~ capture(oneOrMore(noneOf(chars + "\n") | "@@")) ~> {
      (i, x) => Ast.Block.Text(x.replace("@@", "@"), i)
    }
  }
  def Text = TextNot("@")
  def Code = rule {
    "@" ~ capture(Id | BlockExpr2 | ('(' ~ optional(Exprs) ~ ')'))
  }
  def Header = rule {
    "@" ~ capture(Def | Import)
  }

  def HeaderBlock: Rule1[Ast.Header] = rule{
    Header ~ zeroOrMore(capture(NewlineS) ~ Header ~> (_ + _)) ~ runSubParser{new Parser(_, indent, cursor).Body0} ~> {
      (start: String, heads: Seq[String], body: Ast.Block) => Ast.Header(start + heads.mkString, body)
    }
  }

  def BlankLine = rule{ '\n' ~ zeroOrMore(' ') ~ &('\n') }
  def IndentSpaces = rule{ indent.times(' ') ~ zeroOrMore(' ') }
  def Indent = rule{ '\n' ~ IndentSpaces }
  def LoneScalaChain: Rule2[Ast.Block.Text, Ast.Chain] = rule {
    (push(offsetCursor) ~ capture(Indent) ~> ((i, x) => Ast.Block.Text(x, i))) ~
    ScalaChain ~
    IndentBlock ~> {
      (chain: Ast.Chain, body: Ast.Block) => chain.copy(parts = chain.parts :+ body)
    }
  }
  def IndentBlock = rule{
    &("\n") ~
    test(cursorNextIndent() > indent) ~
    runSubParser(new Parser(_, cursorNextIndent(), cursor).Body)
  }
  def IfHead = rule{ "@" ~ capture("if" ~ "(" ~ Expr ~ ")") }
  def IfElse1 = rule{
    push(offsetCursor) ~ IfHead ~ BraceBlock ~ optional("else" ~ (BraceBlock | IndentBlock))
  }
  def IfElse2 = rule{
    Indent ~ push(offsetCursor) ~ IfHead ~ IndentBlock ~ optional(Indent ~ "@else" ~ (BraceBlock | IndentBlock))
  }
  def IfElse = rule{
    (IfElse1 | IfElse2) ~> ((a, b, c, d) => Ast.Block.IfElse(b, c, d, a))
  }

  def ForHead = rule{
    push(offsetCursor) ~ "@" ~ capture("for" ~ '(' ~ Enumerators ~ ')')
  }
  def ForLoop = rule{
    ForHead ~
    BraceBlock ~> ((a, b, c) => Ast.Block.For(b, c, a))
  }
  def LoneForLoop = rule{
    (push(offsetCursor) ~ capture(Indent) ~> ((i, t) => Ast.Block.Text(t, i))) ~
    ForHead ~
    IndentBlock ~>
    ((a, b, c) => Ast.Block.For(b, c, a))
  }

  def ScalaChain = rule {
    push(offsetCursor) ~ Code ~ zeroOrMore(Extension) ~> { (a, b, c) => Ast.Chain(b, c, a)}
  }
  def Extension: Rule1[Ast.Chain.Sub] = rule {
    (push(offsetCursor) ~ '.' ~ capture(Id) ~> ((x, y) => Ast.Chain.Prop(y, x))) |
    (push(offsetCursor) ~ capture(TypeArgs2) ~> ((x, y) => Ast.Chain.TypeArgs(y, x))) |
    (push(offsetCursor) ~ capture(ArgumentExprs2) ~> ((x, y) => Ast.Chain.Args(y, x))) |
    BraceBlock
  }
  def Ws = Whitespace
  // clones of the version in ScalaSyntax, but without tailing whitespace or newlines
  def TypeArgs2 = rule { '[' ~ Ws ~ Types ~ ']' }
  def ArgumentExprs2 = rule {
    '(' ~ Ws ~
    (optional(Exprs ~ ',' ~ Ws) ~ PostfixExpr ~ ':' ~ Ws ~ '_' ~ Ws ~ '*' ~ Ws | optional(Exprs) ) ~
    ')'
  }
  def BlockExpr2: Rule0 = rule { '{' ~ Ws ~ (CaseClauses | Block) ~ '}' }
  def BraceBlock: Rule1[Ast.Block] = rule{ '{' ~ BodyNoBrace ~ '}' }

  def BodyItem(exclusions: String): Rule1[Seq[Ast.Block.Sub]] = rule{
    ForLoop ~> (Seq(_)) |
    LoneForLoop ~> (Seq(_, _)) |
    IfElse ~> (Seq(_)) |
    LoneScalaChain ~> (Seq(_, _)) |
    HeaderBlock ~> (Seq(_)) |
    TextNot("@" + exclusions) ~> (Seq(_)) |
    (push(offsetCursor) ~ capture(Indent) ~> ((i, x) => Seq(Ast.Block.Text(x, i)))) |
    (push(offsetCursor) ~ capture(BlankLine) ~> ((i, x) => Seq(Ast.Block.Text(x, i)))) |
    ScalaChain ~> (Seq(_: Ast.Block.Sub))
  }
  def Body = rule{ BodyEx() }
  def BodyNoBrace = rule{ BodyEx("}") }
  def BodyEx(exclusions: String = "") = rule{
    push(offsetCursor) ~ oneOrMore(BodyItem(exclusions)) ~> {(i, x) =>
      Ast.Block(x.flatten, i)
    }
  }
  def Body0 = rule{
    push(offsetCursor) ~ zeroOrMore(BodyItem("")) ~> {(i, x) =>
      Ast.Block(x.flatten, i)
    }
  }
}

trait Ast{
  def offset: Int
}
object Ast{

  /**
   * @param parts The various bits of text and other things which make up this block
   * @param offset
   */
  case class Block(parts: Seq[Block.Sub],
                   offset: Int = 0)
                   extends Chain.Sub with Block.Sub
  object Block{
    trait Sub extends Ast
    case class Text(txt: String, offset: Int = 0) extends Block.Sub
    case class For(generators: String, block: Block, offset: Int = 0) extends Block.Sub
    case class IfElse(condition: String, block: Block, elseBlock: Option[Block], offset: Int = 0) extends Block.Sub
  }
  case class Header(front: String, block: Block, offset: Int = 0) extends Block.Sub with Chain.Sub

  /**
   * @param lhs The first expression in this method-chain
   * @param parts A list of follow-on items chained to the first
   * @param offset
   */
  case class Chain(lhs: String, parts: Seq[Chain.Sub], offset: Int = 0) extends Block.Sub
  object Chain{
    trait Sub extends Ast
    case class Prop(str: String, offset: Int = 0) extends Sub
    case class TypeArgs(str: String, offset: Int = 0) extends Sub
    case class Args(str: String, offset: Int = 0) extends Sub
  }
}
