package com.dividezero.stubby.test

import com.dividezero.stubby.test.support.StubbyTest
import com.dividezero.stubby.core.util.OptionUtils
import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VersionTest extends FunSuite with StubbyTest with OptionUtils {

  test("Version endpoint should return SBT version") {
    def resp = GET("/_control/version").send.assertOk.asJson[Map[String,String]]
    resp should contain key ("version")
  }

}
