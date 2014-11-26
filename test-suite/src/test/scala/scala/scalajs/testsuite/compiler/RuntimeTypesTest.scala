/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js Test Suite        **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013, LAMP/EPFL        **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */
package scala.scalajs.testsuite.compiler

import scala.scalajs.js
import org.scalajs.jasminetest.JasmineTest

import scala.util.{ Try, Failure }

object RuntimeTypesTest extends JasmineTest {

  describe("scala.Nothing") {

    when("compliant-asinstanceof").
    it("casts to scala.Nothing should fail") {
      val msg = Try("a".asInstanceOf[Nothing]) match {
        case Failure(thr: ClassCastException) => thr.getMessage
        case _ => "not failed"
      }
      expect(msg).toEqual("a is not an instance of scala.runtime.Nothing$")
    }

    it("Array[Nothing] should be allowed to exists and be castable") {
      val arr = Array[Nothing]()
      arr.asInstanceOf[Array[Nothing]]
    }

    it("Array[Array[Nothing]], too") {
      val arr = Array[Array[Nothing]]()
      arr.asInstanceOf[Array[Array[Nothing]]]
      // This apparently works too... Dunno why
      arr.asInstanceOf[Array[Nothing]]
    }

  }

  describe("scala.Null") {

    when("compliant-asinstanceof").
    it("casts to scala.Null should fail for everything else but null") {
      val msg = Try("a".asInstanceOf[Null]) match {
        case Failure(thr: ClassCastException) => thr.getMessage
        case _ => "not failed"
      }
      expect(msg).toEqual("a is not an instance of scala.runtime.Null$")
    }

    it("classTag of scala.Null should contain proper Class[_] - #297") {
      val tag = scala.reflect.classTag[Null]
      expect(tag.runtimeClass != null).toBeTruthy
      expect(tag.runtimeClass.getName).toEqual("scala.runtime.Null$")
    }

    it("casts to scala.Null should succeed on null") {
      null.asInstanceOf[Null]
    }

    it("Array[Null] should be allowed to exist and be castable") {
      val arr = Array.fill[Null](5)(null)
      arr.asInstanceOf[Array[Null]]
    }

    it("Array[Array[Null]] too") {
      val arr = Array.fill[Null](5,5)(null)
      arr.asInstanceOf[Array[Array[Null]]]
      // This apparently works too... Dunno why
      arr.asInstanceOf[Array[Null]]
    }

  }

}
