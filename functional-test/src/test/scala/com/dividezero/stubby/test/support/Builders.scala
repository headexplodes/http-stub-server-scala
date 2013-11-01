package com.dividezero.stubby.test.support

import com.dividezero.stubby.core.util.JsonUtils
import com.dividezero.stubby.test._
import org.apache.http.client.methods._
import com.dividezero.stubby.test.JsonParam
import com.dividezero.stubby.test.JsonRequest
import com.dividezero.stubby.test.JsonResponse
import scala.Some
import com.dividezero.stubby.test.JsonExchange
import org.apache.http.entity.StringEntity
import org.apache.http.{ NameValuePair, HttpEntity }
import org.apache.http.client.utils.{ URIBuilder, URLEncodedUtils }
import org.apache.http.message.BasicNameValuePair

class RequestPatternBuilder(req: JsonRequest = new JsonRequest) extends RequestBuilderBase(req) {

  type Self = RequestPatternBuilder

  def apply(
    method: Option[String] = None,
    path: Option[String] = None,
    params: List[JsonParam] = Nil,
    headers: List[JsonParam] = Nil,
    body: Option[AnyRef] = None,
    bodyType: Option[String] = None): Self =
    new RequestPatternBuilder(new JsonRequest(
      path.orElse(req.path),
      method.orElse(req.method),
      req.params ++ params,
      req.headers ++ headers,
      body.orElse(req.body),
      bodyType.orElse(req.bodyType)))

  def copy(req: JsonRequest): Self = new RequestPatternBuilder(req)

  def bodyType(bodyType: String): Self =
    new RequestPatternBuilder(req.copy(bodyType = Some(bodyType)))

  def bodyRegex(pattern: String): Self = body(pattern).bodyType("regex")
  def bodyJson(pattern: AnyRef): Self = body(pattern).bodyType("json")

  def respond: ResponseBuilder = new ResponseBuilder(req)

}

class ResponseBuilder(exch: JsonExchange) extends ResponseBuilderBase(exch.response) {

  override type Self = ResponseBuilder

  def this(req: JsonRequest, res: JsonResponse = new JsonResponse) = this(new JsonExchange(req, res))

  def apply(status: Option[Int] = None,
    headers: List[JsonParam] = Nil,
    body: Option[AnyRef] = None,
    script: Option[String] = None,
    delay: Option[Int] = None): Self = {
    val newRes = new JsonResponse(
      status orElse exch.response.status,
      exch.response.headers ++ headers,
      body orElse exch.response.body)
    new ResponseBuilder(new JsonExchange(
      exch.request,
      newRes,
      delay orElse exch.delay,
      script orElse exch.script))
  }

  def copy(res: JsonResponse): Self =
    new ResponseBuilder(exch.copy(response = res))

  def withScript(script: String): Self = apply(script = Some(script))
  def withDelay(delay: Int): Self = apply(delay = Some(delay))

  def stub(implicit client: Client) = client.postMessage(exch)

}

abstract class MessageBuilderBase(msg: JsonMessage) {

  type Self <: MessageBuilderBase

  def copy(msg: JsonMessage): Self

  def header(param: JsonParam): Self = copy(msg.copyWith(headers = List(param)))
  def header(name: String, value: String): Self = header(new JsonParam(name, value))
  def header(pair: (String, String)): Self = header(new JsonParam(pair))

  def body(body: AnyRef): Self = copy(msg.copyWith(body = Some(body)))

  def contentType(value: String): Self = header("Content-Type", value)

}

abstract class ResponseBuilderBase(res: JsonResponse) extends MessageBuilderBase(res) {

  type Self <: ResponseBuilderBase

  def copy(res: JsonResponse): Self
  def copy(msg: JsonMessage): Self =
    copy(res.copy(headers = msg.headers, body = msg.body))

  def status(status: Int): Self = copy(res.copy(status = Some(status)))

}

abstract class RequestBuilderBase(req: JsonRequest) extends MessageBuilderBase(req) {

  type Self <: RequestBuilderBase

  def copy(req: JsonRequest): Self
  def copy(msg: JsonMessage): Self =
    copy(req.copy(headers = msg.headers, body = msg.body))

  def param(param: JsonParam): Self = copy(req.copy(params = List(param)))
  def param(name: String, value: String): Self = param(new JsonParam(name, value))
  def param(pair: (String, String)): Self = param(new JsonParam(pair))

  def method(method: String): Self = copy(req.copy(method = Some(method)))
  def path(path: String): Self = copy(req.copy(path = Some(path)))

}

class RequestBuilder(req: JsonRequest) extends RequestBuilderBase(req) {

  override type Self = RequestBuilder

  def this(method: String) = this(new JsonRequest(method = Some(method)))
  def this() = this(new JsonRequest) // blank request

  def apply(
    path: Option[String] = None,
    params: List[JsonParam] = Nil,
    headers: List[JsonParam] = Nil,
    body: Option[AnyRef] = None,
    method: Option[String] = None): Self =
    new RequestBuilder(new JsonRequest(
      path orElse req.path,
      method orElse req.method,
      req.params ++ params,
      req.headers ++ headers,
      body.orElse(req.body)))

  def copy(req: JsonRequest): Self = new RequestBuilder(req)

  def send(implicit client: Client): GenericClientResponse = {
    val apacheReq = makeApacheReq(req.method)

    val uri = makeUri(req.path)

    makeQuery(req.params) foreach {
      q => uri.setQuery(q)
    }

    apacheReq.setURI(client.makeUri(uri.build))

    req.headers foreach { h =>
      apacheReq.addHeader(h.name, h.value)
    }

    makeEntity(req.body) foreach { e =>
      apacheReq match {
        case base: HttpEntityEnclosingRequestBase => base.setEntity(e) // <HACK/>
      }
    }

    client.execute(apacheReq)
  }

  private def makeApacheReq(method: Option[String]) = method match {
    case Some("GET") => new HttpGet()
    case Some("POST") => new HttpPost()
    case Some("PUT") => new HttpPut()
    case Some("DELETE") => new HttpDelete()
    case Some(_) => throw new UnsupportedOperationException("Need to implement method")
    case None => throw new RuntimeException("Request requires a method")
  }

  private def makeEntity(body: Option[AnyRef]): Option[HttpEntity] = body match {
    case Some(str: String) => Some(new StringEntity(str))
    case Some(map: Map[_, _]) => Some(new StringEntity(JsonUtils.serialize(map)))
    case Some(lst: List[_]) => Some(new StringEntity(JsonUtils.serialize(lst)))
    case Some(_) => throw new RuntimeException("Unknown body type")
    case None => None
  }

  private def makeQuery(params: List[JsonParam]): Option[String] = {
    if (params.isEmpty) None
    else {
      import scala.collection.JavaConversions._
      val pairs = params.map(p => new BasicNameValuePair(p.name, p.value))
      Some(URLEncodedUtils.format(pairs, "UTF-8"))
    }
  }

  private def makeUri(path: Option[String]): URIBuilder = path match {
    case Some(p) => new URIBuilder().setPath(p)
    case None => throw new RuntimeException("Request requires a path")
  }

}

trait RequestBuilders {

  def GET = new RequestBuilder("GET")
  def POST = new RequestBuilder("POST")
  def PUT = new RequestBuilder("PUT")
  def DELETE = new RequestBuilder("DELETE")

}

trait RequestPatternBuilders {
  def given: RequestPatternBuilder = new RequestPatternBuilder
}
