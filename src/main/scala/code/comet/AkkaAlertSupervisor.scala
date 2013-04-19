package code.comet

import akka.actor._
import scala.concurrent.Await
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class AkkaAlertSupervisor extends Actor {
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    case _: Exception =>
      println("Child died now resuming the child...")
      Resume
  }
  def receive = {
    case props: Props => sender ! context.actorOf(props)
  }
}