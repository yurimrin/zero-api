package com.zero

import akka.actor._
import akka.util.Timeout

object Conversation {
  def props(implicit timeout: Timeout) = Props(new Conversation)
  def name = "conversion"

  case class Send(message: String)
  case class Replay(message: String)
}

class Conversation(implicit timeout: Timeout) extends Actor {
  import Conversation._

  def receive = {
    case Send(message) => {
      sender() ! Conversation.Replay(s"replay:${message}")
    }
  }
}

