package com.dz.stubby.core.js

import org.scalatest.FunSuite
import com.dz.stubby.core.model.StubRequest
import com.dz.stubby.core.model.StubResponse
import com.dz.stubby.core.model.StubExchange
import com.dz.stubby.core.model.StubParam

class ScriptWorldTest extends FunSuite {

  val request = new StubRequest(path = "/path")
  val response = new StubResponse(status = 200)

  test("create world from exchange") {
    val world = new ScriptWorld(request, response, Some(1234L))
    
    assert(world.getDelay === 1234)
    assert(world.getRequest.getPath === "/path")
    assert(world.getResponse.getStatus === 200)
  }

  test("generate updated exchange") {
      val world = new ScriptWorld(request, response, Some(1234L))
      
      world.setDelay(4321)
      world.getResponse.setStatus(500)
      world.getResponse.setBody("<body/>")
      world.getResponse.getHeaders.add(StubParam("X-Foo", "bar"))
      
      val (result, delay) = world.result
      
      assert(delay.get === 4321)
      assert(result.status === 500)
      assert(result.body === "<body/>")
      assert(result.headers === List(StubParam("X-Foo", "bar")))
  }

}