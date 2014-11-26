package scala.scalajs.tools.io

import java.io._
import java.net.URI

import scala.scalajs.ir
import scala.scalajs.tools.sourcemap._

/** A virtual input file.
 */
trait VirtualFile {
  /** Path of the file, including everything.
   *  Unique if possible (used for lookup). */
  def path: String

  /** Name of the file/writer, including extension */
  def name: String = VirtualFile.nameFromPath(path)

  /** Optionally returns an implementation-dependent "version" token.
   *  Versions are compared with ==.
   *  If non-empty, a different version must be returned when the content
   *  changes. It should be equal if the content has not changed, but it is
   *  not mandatory.
   *  Such a token can be used by caches: the file need not be read and
   *  processed again if its version has not changed.
   */
  def version: Option[String] = None

  /** Whether this file exists. Reading a non-existent file may fail */
  def exists: Boolean

  /** URI for this virtual file */
  def toURI: URI = {
    new URI(
        "virtualfile", // Pseudo-Scheme
        path,          // Scheme specific part
        null           // Fragment
    )
  }
}

object VirtualFile {
  /** Splits at the last slash and returns remainder */
  def nameFromPath(path: String): String = {
    val pos = path.lastIndexOf('/')
    if (pos == -1) path
    else path.substring(pos + 1)
  }
}

/** A virtual input file.
 */
trait VirtualTextFile extends VirtualFile {
  /** Returns the content of the file. */
  def content: String

  /** Returns a new Reader of the file. */
  def reader: Reader = new StringReader(content)

  /** Returns the lines in the content.
   *  Lines do not contain the new line characters.
   */
  def readLines(): List[String] = IO.readLines(reader)
}

object VirtualTextFile {
  def empty(path: String): VirtualTextFile =
    new MemVirtualTextFile(path)
}

trait WritableVirtualTextFile extends VirtualTextFile {
  def contentWriter: Writer
}

/** A virtual binary input file.
 */
trait VirtualBinaryFile extends VirtualFile {
  /** Returns the content of the file. */
  def content: Array[Byte]

  /** Returns a new InputStream of the file. */
  def inputStream: InputStream = new ByteArrayInputStream(content)
}

/** A virtual input file which contains JavaScript code.
 *  It may have a source map associated with it.
 */
trait VirtualJSFile extends VirtualTextFile {
  /** Optionally, content of the source map file associated with this
   *  JavaScript source.
   */
  def sourceMap: Option[String] = None
}

object VirtualJSFile {
  def empty(path: String): VirtualJSFile =
    new MemVirtualJSFile(path).withVersion(Some(path))
}

trait WritableVirtualJSFile extends WritableVirtualTextFile with VirtualJSFile {
  def sourceMapWriter: Writer
}

/** A virtual Scala.js IR file.
 *  It contains the class info and the IR tree.
 */
trait VirtualScalaJSIRFile extends VirtualFile {
  /** Rough class info of this file. */
  def roughInfo: ir.Infos.RoughClassInfo = info

  /** Class info of this file. */
  def info: ir.Infos.ClassInfo =
    infoAndTree._1

  /** IR Tree of this file. */
  def tree: ir.Trees.ClassDef =
    infoAndTree._2

  /** Class info and IR tree of this file. */
  def infoAndTree: (ir.Infos.ClassInfo, ir.Trees.ClassDef)
}

/** Base trait for virtual Scala.js IR files that are serialized as binary file.
 */
trait VirtualSerializedScalaJSIRFile extends VirtualBinaryFile with VirtualScalaJSIRFile {
  /** Rough class info of this file. */
  override def roughInfo: ir.Infos.RoughClassInfo = {
    // Overridden to read only the necessary parts
    val stream = inputStream
    try {
      ir.InfoSerializers.deserializeRoughInfo(stream)
    } catch {
      case e: IOException =>
        throw new IOException(s"Failed to deserialize rough info of $path", e)
    } finally {
      stream.close()
    }
  }

  /** Class info of this file. */
  override def info: ir.Infos.ClassInfo = {
    // Overridden to read only the necessary parts
    val stream = inputStream
    try {
      ir.InfoSerializers.deserializeFullInfo(stream)
    } catch {
      case e: IOException =>
        throw new IOException(s"Failed to deserialize info of $path", e)
    } finally {
      stream.close()
    }
  }

  /** Class info and IR tree of this file. */
  override def infoAndTree: (ir.Infos.ClassInfo, ir.Trees.ClassDef) = {
    val stream = inputStream
    try {
      val (version, info) = ir.InfoSerializers.deserializeVersionFullInfo(stream)
      val tree = ir.Serializers.deserialize(
          stream, version).asInstanceOf[ir.Trees.ClassDef]
      (info, tree)
    } catch {
      case e: IOException =>
        throw new IOException(s"Failed to deserialize $path", e)
    } finally {
      stream.close()
    }
  }
}
