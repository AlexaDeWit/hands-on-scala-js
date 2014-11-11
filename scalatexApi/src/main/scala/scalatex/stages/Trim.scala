package scalatex.stages
import acyclic.file

/**
 * Preprocesses the input string to normalize things related to whitespace
 *
 * Find the "first" non-whitespace-line of the text and remove the front
 * of every line to align that first line with the left margin.
 *
 * Remove all trailing whitespace from each line.
 */
object Trim extends (String => (String, Int)){
  def apply(str: String) = {
    val lines = str.split("\n", -1)
    val offset = lines.iterator
                      .filter(_.length > 0)
                      .next()
                      .takeWhile(_ == ' ')
                      .length
    val res = lines.iterator
                   .map(_.replaceFirst("\\s+$", ""))
                   .mkString("\n")
    (res, offset)
  }
  def old(str: String) = {
    val (res, offset) = this.apply(str)
    res.split("\n", -1).map(_.drop(offset)).mkString("\n")
  }
}
