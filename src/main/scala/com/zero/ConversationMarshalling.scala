package com.zero

import java.sql.Timestamp
import java.text.SimpleDateFormat

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

case class SendMessage(message: String) {
  require(message.length > 0)
}

case class Error(message: String)

case class ZeroRequestJson(
  language: String,
  botId: String,
  appId: String,
  voiceText: String,
  appRecvTime: String,
  appSendTime: String
)
object ZeroRequestJson {
  def apply(appId: String, message: String) = {
    val now = new Timestamp(System.currentTimeMillis())
    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val nowString = simpleDateFormat.format(now.getTime)
    new ZeroRequestJson(
      "ja-JP",
      "Chatting",
      appId,
      message,
      nowString,
      nowString
    )
  }
}

case class ZeroSystemTextResponseJson(
  expression: String,
  utterance: String
)
case class ZeroResponseJson(
  systemText: ZeroSystemTextResponseJson
)

trait ConversationMarshalling  extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val sendMessageFormat = jsonFormat1(SendMessage.apply)
  implicit val errorFormat = jsonFormat1(Error.apply)
  implicit val zeroRequestJsonFormat = jsonFormat6(ZeroRequestJson.apply)
  implicit val zeroSystemTextResponseJsonFormat = jsonFormat2(ZeroSystemTextResponseJson.apply)
  implicit val zeroResponseJsonFormat = jsonFormat1(ZeroResponseJson.apply)
}
