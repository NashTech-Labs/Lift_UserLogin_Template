package code.comet

import akka.actor._
import scala.concurrent.Await
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object CometAlertController {
  implicit val timeout = Timeout(5 seconds)
  val system = ActorSystem("manager")
  val supervisor = system.actorOf(Props[AkkaAlertSupervisor])

  val AlertActor = {
    val future = supervisor ? Props[AkkaAlertActor]
    val actor = Await.result(future, timeout.duration).asInstanceOf[ActorRef]
    actor
  }

  def getManager(): ActorRef = {
    AlertActor
  }
}