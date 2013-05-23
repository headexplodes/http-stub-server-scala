package com.dz.stubby.core

import scala.collection.mutable.Stack

class NotFoundException(message: String) extends RuntimeException(message)

class RequestPattern(request: StubRequest) {
  def findMatch(message: StubRequest): MatchResult = new MatchResult
}

class MatchResult {
  def score: Int = 0
  def matches: Boolean = false
}

class StubServiceResult( // returned by the 'findMatch' method
    val attempts: List[MatchResult],
    val response: StubResponse,
    val delay: Int) {
  def this(attempts: List[MatchResult]) = this(attempts, null, 0)
  def matchFound(): Boolean = attempts.exists(_.matches)
}

trait BodyPattern {
  def matches(request: StubMessage): MatchField
}

public abstract class BodyPattern { // Note: using abstract class so we can force override of equals()
    
    public abstract MatchField matches(StubMessage request);

    @Override
    public abstract boolean equals(Object object); // force implementors to override
    
    @Override
    public abstract int hashCode(); // force implementors to override

}

class StubServiceExchange(val exchange: StubExchange) { // wrap exchange model with some extra runtime info

  val requestPattern: RequestPattern = new RequestPattern(exchange.request)
  val attempts: Stack[MatchResult] = new Stack

  def matches(message: StubRequest): MatchResult = {
    val result = requestPattern.findMatch(message)
    if (result.score >= 5) { // only record attempts that match request path
      attempts.push(result)
    }
    result
  }

  override def toString = requestPattern.toString
  override def hashCode = requestPattern.hashCode(); // hash/equality is based on the request pattern only
  override def equals(obj: Object) = obj match {
    case e: StubServiceExchange => e.requestPattern.equals(requestPattern)
    case _ => false
  }

}

class StubService {

  val requests: LinkedList[StubRequest] = new LinkedList
  val responses: LinkedList[StubServiceExchange] = new LinkedList

  def addResponse(exchange: StubExchange): Unit = {
    val internal = new StubServiceExchange(exchange);

    //responses.remove(internal); // remove existing stubed request (ie, will never match anymore)

    responses.update(internal, new LinkedList(responses.tail)); // ensure most recent match first   
  }

}

/*




    public synchronized StubServiceResult findMatch(StubRequest request) {
        try {
            LOGGER.trace("Got request: " + JsonUtils.prettyPrint(request));
            requests.addFirst(request);
            List<MatchResult> attempts = new ArrayList<MatchResult>();
            for (StubServiceExchange response : responses) {
                MatchResult matchResult = response.matches(request);
                attempts.add(matchResult);
                if (matchResult.matches()) {
                    LOGGER.info("Matched: " + request.getPath() + "");
                    StubExchange exchange = response.getExchange();
                    if (exchange.getScript() != null) {
                        ScriptWorld world = new ScriptWorld(request, exchange); // creates deep copies of objects
                        new Script(exchange.getScript()).execute(world);
                        return new StubServiceResult(
                                attempts, world.getResponse(), world.getDelay());
                    } else {
                        return new StubServiceResult(
                                attempts, exchange.getResponse(), exchange.getDelay());
                    }
                }
            }
            LOGGER.info("Didn't match: " + request.getPath());
            this.notifyAll(); // inform any waiting threads that a new request has come in
            return new StubServiceResult(attempts); // no match (empty list)
        } catch (Exception e) {
            throw new RuntimeException("Error matching request", e);
        }
    }

    public synchronized StubServiceExchange getResponse(int index) throws NotFoundException {
        try {
            return responses.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new NotFoundException("Response does not exist: " + index);
        }
    }

    public synchronized List<StubServiceExchange> getResponses() {
        return responses;
    }

    public synchronized void deleteResponse(int index) throws NotFoundException {
        LOGGER.trace("Deleting response: " + index);
        try {
            responses.remove(index);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Response does not exist: " + index);
        }
    }

    public synchronized void deleteResponses() {
        LOGGER.trace("Deleting all responses");
        responses.clear();
    }

    public synchronized StubRequest getRequest(int index) throws NotFoundException {
        try {
            return requests.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new NotFoundException("Response does not exist: " + index);
        }
    }

    public synchronized List<StubRequest> findRequests(StubRequest filter, long timeout) { // blocking call
        long remaining = timeout;
        while (remaining > 0) {
            List<StubRequest> result = findRequests(filter);
            if (result.isEmpty()) {
                try {
                    long start = System.currentTimeMillis();
                    this.wait(remaining); // wait for a request to come in, or time to expire
                    remaining -= System.currentTimeMillis() - start;
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while waiting for request");
                }
            } else {
                return result;
            }
        }
        return Collections.emptyList();
    }

    public synchronized List<StubRequest> findRequests(StubRequest filter) {
        RequestPattern pattern = new RequestPattern(filter);
        List<StubRequest> result = new ArrayList<StubRequest>();
        for (StubRequest request : requests) {
            if (pattern.match(request).matches()) {
                result.add(request);
            }
        }
        return result;
    }

    public synchronized List<StubRequest> getRequests() {
        return requests;
    }

    public synchronized void deleteRequest(int index) throws NotFoundException {
        LOGGER.trace("Deleting request: " + index);
        try {
            requests.remove(index);
        } catch (IndexOutOfBoundsException e) {
            throw new NotFoundException("Request does not exist: " + index);
        }
    }

    public synchronized void deleteRequests() {
        LOGGER.trace("Deleting all requests");
        requests.clear();
    }

*/