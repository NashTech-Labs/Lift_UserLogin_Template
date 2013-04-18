package code.comet

import akka.actor._

object CometAlertController {

  val system = ActorSystem("manager")

  val AlertActor = system.actorOf(Props[AkkaAlertActor])

  def getManager(): ActorRef = {
    AlertActor
  }

}