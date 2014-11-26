/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js IR                **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2014, LAMP/EPFL        **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */


package scala.scalajs.ir

import scala.annotation.switch

sealed abstract class ClassKind {
  import ClassKind._

  def isClass = this match {
    case Class | ModuleClass => true
    case _                   => false
  }

  def isType = this match {
    case TraitImpl => false
    case _         => true
  }
}

object ClassKind {
  case object Class extends ClassKind
  case object ModuleClass extends ClassKind
  case object Interface extends ClassKind
  case object RawJSType extends ClassKind
  case object HijackedClass extends ClassKind
  case object TraitImpl extends ClassKind

  private[ir] def toByte(kind: ClassKind): Byte = kind match {
    case ClassKind.Class         => 1
    case ClassKind.ModuleClass   => 2
    case ClassKind.Interface     => 3
    case ClassKind.RawJSType     => 4
    case ClassKind.HijackedClass => 5
    case ClassKind.TraitImpl     => 6
  }

  private[ir] def fromByte(b: Byte): ClassKind = (b: @switch) match {
    case 1 => ClassKind.Class
    case 2 => ClassKind.ModuleClass
    case 3 => ClassKind.Interface
    case 4 => ClassKind.RawJSType
    case 5 => ClassKind.HijackedClass
    case 6 => ClassKind.TraitImpl
  }
}
