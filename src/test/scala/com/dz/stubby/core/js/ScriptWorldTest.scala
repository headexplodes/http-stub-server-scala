package com.dz.stubby.core.js

import org.scalatest.FunSuite
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubParam

class ScriptWorldTest extends FunSuite {

  val request = new StubRequest(path = "/path")
  val response = new StubResponse(status = 200)
  val exchange = new StubExchange(request, response, 1234, "script()")

  test("create world from exchange") {
    val world = new ScriptWorld(exchange)
    
    assert(world.getDelay === 1234)
    assert(world.getRequest.getPath === "/path")
    assert(world.getResponse.getStatus === 200)
  }

  test("generate updated exchange") {
      val world = new ScriptWorld(exchange)
      
      world.setDelay(4321)
      world.getResponse.setStatus(500)
      world.getResponse.setBody("<body/>")
      world.getResponse.getHeaders.add(StubParam("X-Foo", "bar"))
      
      val generated = world.toStubExchange
      
      assert(generated.delay === 4321)
      assert(generated.response.status === 500)
      assert(generated.response.body === "<body/>")
      assert(generated.response.headers === List(StubParam("X-Foo", "bar")))
      assert(generated.request.path === "/path")
  }

}