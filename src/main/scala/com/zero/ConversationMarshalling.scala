package com.zero

import spray.json._

case class SendMessage(message: String) {
  require(message.length > 0)
}

case class Error(message: String)


trait ConversationMarshalling  extends DefaultJsonProtocol {
  implicit val sendMessageFormat = jsonFormat1(SendMessage)
  implicit val errorFormat = jsonFormat1(Error)
}
