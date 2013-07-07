package com.dz.stubby.core.service

import com.dz.stubby.core.service.model._
import com.dz.stubby.core.model._
import scala.collection.mutable.ListBuffer
import com.dz.stubby.core.js.ScriptWorld
import com.dz.stubby.core.js.Script

case class NotFoundException(message: String) extends RuntimeException(message)

class StubService {

  val requests: ListBuffer[StubRequest] = new ListBuffer
  val responses: ListBuffer[StubServiceExchange] = new ListBuffer

  def addResponse(exchange: StubExchange): Unit = {
    val internal = new StubServiceExchange(exchange)
    responses -= internal // remove existing stubed request (ie, will never match anymore)
    internal +=: responses // ensure most recent matched first   
  }

  def findMatch(request: StubRequest): StubServiceResult = this.synchronized {
    try {
      //LOGGER.trace("Got request: " + JsonUtils.prettyPrint(request))
      request +=: requests // prepend
      val attempts = new ListBuffer[MatchResult]
      for (response <- responses) {
        val matchResult = response.matches(request)
        attempts += matchResult
        if (matchResult.matches) {
          //LOGGER.info("Matched: " + request.getPath() + "")
          val exchange = response.exchange
          return exchange.script match {
            case Some(script) => {
                val world = new ScriptWorld(request, exchange.response, exchange.delay) // creates deep copies of objects
                new Script(script).execute(world)
                val (scriptResponse, scriptDelay) = world.result
                new StubServiceResult(
                  attempts.toList, Some(scriptResponse), scriptDelay)
            }
            case None => new StubServiceResult(
              attempts.toList, Some(exchange.response), exchange.delay)
          }
        }
      }
      //LOGGER.info("Didn't match: " + request.getPath())
      this.notifyAll // inform any waiting threads that a new request has come in
      new StubServiceResult(Nil) // no match (empty list)
    } catch {
      case e: Exception =>
        throw new RuntimeException("Error matching request", e)
    }
  }

  @throws[NotFoundException]("if index does not exist")
  def getResponse(index: Int): StubServiceExchange = this.synchronized {
    try {
      return responses(index)
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new NotFoundException("Response does not exist: " + index)
    }
  }

  def deleteResponse(index: Int) = this.synchronized {
    //LOGGER.trace("Deleting response: " + index)
    try {
      responses.remove(index)
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new RuntimeException("Response does not exist: " + index)
    }
  }

  def deleteResponses() = this.synchronized {
    //LOGGER.trace("Deleting all responses")
    responses.clear
  }

  @throws[NotFoundException]("if index does not exist")
  def getRequest(index: Int): StubRequest = this.synchronized {
    try {
      requests(index)
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new NotFoundException("Response does not exist: " + index)
    }
  }

  def findRequests(filter: StubRequest, timeout: Long): Traversable[StubRequest] = this.synchronized { // blocking call
    var remaining: Long = timeout // TODO: refactor to make more functional...
    while (remaining > 0) {
      val result = findRequests(filter)
      if (result.isEmpty) {
        try {
          val start = System.currentTimeMillis
          this.wait(remaining) // wait for a request to come in, or time to expire
          remaining -= System.currentTimeMillis - start
        } catch {
          case e: InterruptedException =>
            throw new RuntimeException("Interrupted while waiting for request")
        }
      } else {
        return result
      }
    }
    return Nil
  }

  def findRequests(filter: StubRequest): Traversable[StubRequest] = this.synchronized {
    val pattern = new RequestPattern(filter)
    requests.filter(r => pattern.matches(r).matches)
  }

  @throws[NotFoundException]("if index does not exist")
  def deleteRequest(index: Int) = this.synchronized {
    //LOGGER.trace("Deleting request: " + index)
    try {
      requests.remove(index)
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new NotFoundException("Request does not exist: " + index)
    }
  }

  def deleteRequests() = this.synchronized {
    //LOGGER.trace("Deleting all requests")
    requests.clear
  }

}







