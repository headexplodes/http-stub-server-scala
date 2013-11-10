package com.dividezero.stubby.core.js

import javax.script.ScriptException
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import org.scalatest.FunSuite
import javax.script.SimpleScriptContext
import javax.script.ScriptContext
import scala.collection.mutable.ListBuffer

class ScriptEngineTest extends FunSuite { // test that the JavaScript engine works they way we think

  private def createEngine: ScriptEngine = {
    val manager = new ScriptEngineManager()
    val engine = manager.getEngineByName("JavaScript")
    engine.setContext(new SimpleScriptContext)
    engine
  }

  test("return nothing") {
    assert(createEngine.eval("") === null)
  }

  test("return boolean") {
    assert(createEngine.eval("true") === true)
  }

  test("return integer") {
    assert(createEngine.eval("1234") === 1234)
  }

  test("return float") {
    assert(createEngine.eval("1.234") === 1.234)
  }

  test("return string") {
    assert(createEngine.eval("\"foo\"") === "foo")
  }

//  test("get list item") {
//    val engine = createEngine
//    val list = ListBuffer(1, 2)
//    engine.put("list", list)
//    engine.eval("list.add(3)")
//    assert(list === List(1, 2, 3))
//  }

}