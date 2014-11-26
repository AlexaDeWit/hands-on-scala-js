/* Scala.js compiler
 * Copyright 2013 LAMP/EPFL
 * @author  Sébastien Doeraene
 */

package scala.scalajs.compiler

import scala.tools.nsc._

import scala.collection.mutable

/** Extension of ScalaPrimitives for primitives only relevant to the JS backend
 *
 *  @author Sébastie Doeraene
 */
abstract class JSPrimitives {
  val global: Global

  type ThisJSGlobalAddons = JSGlobalAddons {
    val global: JSPrimitives.this.global.type
  }

  val jsAddons: ThisJSGlobalAddons

  import global._
  import jsAddons._
  import definitions._
  import rootMirror._
  import jsDefinitions._
  import scalaPrimitives._

  val GETCLASS = 301 // Object.getClass()

  val F2JS = 305     // FunctionN to js.FunctionN
  val F2JSTHIS = 306 // FunctionN to js.ThisFunction{N-1}

  val DYNNEW = 321 // Instantiate a new JavaScript object

  val DYNSELECT = 330 // js.Dynamic.selectDynamic
  val DYNUPDATE = 331 // js.Dynamic.updateDynamic
  val DYNAPPLY = 332  // js.Dynamic.applyDynamic
  val DYNLITN = 333   // js.Dynamic.literal.applyDynamicNamed
  val DYNLIT = 334    // js.Dynamic.literal.applyDynamic

  val DICT_DEL = 335   // js.Dictionary.delete

  val ARR_CREATE = 337 // js.Array.apply (array literal syntax)

  val UNDEFVAL = 342  // js.undefined
  val ISUNDEF = 343   // js.isUndefined
  val TYPEOF = 344    // typeof x
  val DEBUGGER = 345  // js.debugger()
  val HASPROP = 346   // js.Object.hasProperty(o, p), equiv to `p in o` in JS
  val OBJPROPS = 347  // js.Object.properties(o), equiv to `for (p in o)` in JS
  val JS_NATIVE = 348 // js.native. Marker method. Fails if tried to be emitted.

  val UNITVAL = 349  // () value, which is undefined
  val UNITTYPE = 350 // BoxedUnit.TYPE (== classOf[Unit])

  val ENV_INFO = 353  // __ScalaJSEnv via helper

  /** Initialize the map of primitive methods (for GenJSCode) */
  def init(): Unit = initWithPrimitives(addPrimitive)

  /** Init the map of primitive methods for Scala.js (for PrepJSInterop) */
  def initPrepJSPrimitives(): Unit = {
    scalaJSPrimitives.clear()
    initWithPrimitives(scalaJSPrimitives.put)
  }

  /** Only call from PrepJSInterop. In GenJSCode, use
   *  scalaPrimitives.isPrimitive instead
   */
  def isJavaScriptPrimitive(sym: Symbol): Boolean =
    scalaJSPrimitives.contains(sym)

  private val scalaJSPrimitives = mutable.Map.empty[Symbol, Int]

  private def initWithPrimitives(addPrimitive: (Symbol, Int) => Unit): Unit = {
    addPrimitive(Object_getClass, GETCLASS)

    for (i <- 0 to 22)
      addPrimitive(JSAny_fromFunction(i), F2JS)
    for (i <- 1 to 22)
      addPrimitive(JSThisFunction_fromFunction(i), F2JSTHIS)

    addPrimitive(JSDynamic_newInstance, DYNNEW)

    addPrimitive(JSDynamic_selectDynamic, DYNSELECT)
    addPrimitive(JSDynamic_updateDynamic, DYNUPDATE)
    addPrimitive(JSDynamic_applyDynamic, DYNAPPLY)
    addPrimitive(JSDynamicLiteral_applyDynamicNamed, DYNLITN)
    addPrimitive(JSDynamicLiteral_applyDynamic, DYNLIT)

    addPrimitive(JSDictionary_delete, DICT_DEL)

    addPrimitive(JSArray_create, ARR_CREATE)

    val ntModule = getRequiredModule("scala.reflect.NameTransformer")

    addPrimitive(JSPackage_typeOf, TYPEOF)
    addPrimitive(JSPackage_debugger, DEBUGGER)
    addPrimitive(JSPackage_undefined, UNDEFVAL)
    addPrimitive(JSPackage_isUndefined, ISUNDEF)
    addPrimitive(JSPackage_native, JS_NATIVE)

    addPrimitive(JSObject_hasProperty, HASPROP)
    addPrimitive(JSObject_properties, OBJPROPS)

    addPrimitive(BoxedUnit_UNIT, UNITVAL)
    addPrimitive(BoxedUnit_TYPE, UNITTYPE)

    addPrimitive(getMember(RuntimePackageModule,
        newTermName("environmentInfo")), ENV_INFO)
  }

  def isJavaScriptPrimitive(code: Int) =
    code >= 300 && code < 360
}
