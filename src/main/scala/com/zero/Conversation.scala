package com.zero

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import spray.json._

import scala.concurrent.duration.Duration

object Conversation {
  def props(implicit timeout: Timeout) = Props(new Conversation)
  def name = "conversion"

  case class Send(message: String)
  case class Replay(message: String)
}

class Conversation(implicit timeout: Timeout) extends Actor with ConversationMarshalling {
  import Conversation._

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load()
  val apikey = config.getString("zero-api.apikey")
  val appId = config.getString("zero-api.appId")
  val requestUri = config.getString("zero-api.request-uri")

  def receive = {
    case Send(message) => {
      val res = requestApi(s"${requestUri}?APIKEY=${apikey}", message)

      sender() ! Conversation.Replay(s"replay:${res}")
    }
  }

  private def requestApi(uri: String, message: String) = {
    val REQUEST_HEADER_LIST = Vector(
      headers.`Accept-Charset`.apply(HttpCharsets.`UTF-8`),
      headers.`Content-Type`(ContentType(MediaTypes.`application/json`))
    )
    val body = ZeroRequestJson(appId, message).toJson
    val httpRequest = HttpRequest(HttpMethods.POST, uri, REQUEST_HEADER_LIST, HttpEntity(ContentTypes.`application/json`, body.compactPrint))

    val responseFuture: Future[HttpResponse] = Http().singleRequest(httpRequest)
    val response = responseFuture.flatMap { case res => {
        Unmarshal(res.entity).to[ZeroResponseJson]
    }}
    val responseJson = Await.result(response, Duration.Inf)
    responseJson.systemText.utterance
  }
}

