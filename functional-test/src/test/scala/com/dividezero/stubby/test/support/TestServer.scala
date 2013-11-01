package com.dividezero.stubby.test.support

import unfiltered.netty.Http
import com.dividezero.stubby.standalone.{AppPlan, Server}
import unfiltered.util.StartableServer

/*
 * Simple test server using standalone implementation
 */
object TestServer {

  val server = new Server(Nil)
  var http: StartableServer = null

  def start() {
    http = Http.anylocal.plan(new AppPlan(server))
    http.start()
  }

  def stop() = http.stop()

  def port: Int = http.port

  def isRunning: Boolean = http != null

}