package com.dz.stubby.core.js

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext
import javax.script.ScriptException

class Script(val source: String) {

  private def createEngine: ScriptEngine = {
    val manager = new ScriptEngineManager()
    val engine = manager.getEngineByName("JavaScript")
    engine.setContext(new SimpleScriptContext())
    engine
  }

  def execute(world: ScriptWorld): AnyRef = {
    val engine = createEngine

    engine.put("request", world.getRequest) // TODO: deprecate (use 'exchange.request')
    engine.put("response", world.getResponse) // TODO: deprecate (use 'exchange.response')
    engine.put("exchange", world)

    try {
      return engine.eval(source) // note: result is actually not used by stub server atm.
    } catch {
      case e: ScriptException =>
        throw new RuntimeException("Error executing script", e)
    }
  }

}