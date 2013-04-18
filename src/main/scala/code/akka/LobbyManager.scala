package code.akka

import akka.actor._
import akka.actor.ActorSystem

class LobbyManager extends Actor {

  def receive = {
    case msg: String => println(msg)
  }

}
