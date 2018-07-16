package com.zero

import scala.concurrent.ExecutionContext

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

class RestApi(system: ActorSystem, timeout: Timeout)
    extends RestRoutes {
  implicit val requestTimeout = timeout
  implicit def executionContext = system.dispatcher

  def createConversation = system.actorOf(Conversation.props, Conversation.name)
}

trait RestRoutes extends ConversationApi
    with ConversationMarshalling {
  import StatusCodes._

  def routes: Route = eventRoute

  def eventRoute =
    pathPrefix("zero") {
      pathEndOrSingleSlash {
        post {
          entity(as[SendMessage]) { sm =>
            onSuccess(send(sm.message)) {
              case Conversation.Replay(message) => complete(OK, message)
              case _ =>
                val err = Error(s"An error occurred.")
                complete(BadRequest, err)
            }
          }
        }
      }
    }

}

trait ConversationApi {
  import Conversation._

  def createConversation(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val conversation = createConversation()

  def send(message: String) =
    conversation.ask(Send(message))
      .mapTo[Replay]
}
