package com.dividezero.stubby.test.support

import com.dividezero.stubby.test._
import org.scalatest.{ShouldMatchers, Assertions, FunSuite, BeforeAndAfter}
import java.net.URI
import org.apache.commons.io.IOUtils
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.{HttpStatus, HttpResponse}
import org.apache.http.client.methods.HttpUriRequest
import com.dividezero.stubby.test.MessageBuilder

trait StubbyTest extends FunSuite
      with BeforeAndAfter
      with ShouldMatchers
      with RequestBuilders
      with RequestPatternBuilders
      with HttpStatuses {
 
  private val TestServerProperty: String = "test.server" // server to test against

  implicit var client: Client = null

  def startStandaloneServer(): String = {
    // keep server running for all tests
    if (!TestServer.isRunning) {
      TestServer.start()
    }
    s"http://localhost:${TestServer.port}"
  }

  before {
    val testServer = sys.props.get(TestServerProperty).getOrElse(startStandaloneServer())
    client = new Client(new URI(testServer))
    client.reset()
  }

  after {
    client.close()
  }

  protected def postFile(filename: String) {
    val path = "/tests/" + filename
    val resource = getClass.getResourceAsStream(path)
    if (resource != null) {
      client.postMessage(IOUtils.toString(resource))
    } else {
      throw new RuntimeException("Resource not found: " + path)
    }
  }

  def makeUri(path: String): URI = client.makeUri(path)

  def close(response: HttpResponse) = HttpClientUtils.closeQuietly(response)

  def execute(request: HttpUriRequest): GenericClientResponse = client.execute(request)

  def responses(): List[JsonStubbedExchange] = client.responses

  def assertOk(response: GenericClientResponse) = assertStatus(HttpStatus.SC_OK, response)

  def assertNotFound(response: GenericClientResponse) = assertStatus(HttpStatus.SC_NOT_FOUND, response)

  def assertStatus(status: Int, response: GenericClientResponse) = assert(status === response.status)

  def builder: MessageBuilder = new MessageBuilder(client)

  def assertHasHeader(request: JsonMessage, name: String, value: String) {
    request.getHeader(name) match {
      case Some(s) => assert(s == value)
      case None => fail("Header not found")
    }
  }

  def assumeNotTravisCi() = assume(!isTravisCi, "Not running as Travis CI")
  def isTravisCi = "true".equals(System.getenv("TRAVIS"))

  def assertTimeTaken(started: Long, ended: Long, expected: Long) {
    def tolerance = 500 // half second tolerance

    if (!isTravisCi) {
      val timeTaken = Math.abs(ended - started)

      // don't assert timing if running on Travis-CI (it's quite slow...)
      timeTaken should be (expected +- tolerance)
    }
  }

  def now() = System.currentTimeMillis()

}