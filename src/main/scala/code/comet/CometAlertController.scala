package code.comet

import akka.actor._
import scala.concurrent.Await
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.pattern.AskTimeoutException
import net.liftweb.common.Loggable
import java.util.concurrent.TimeoutException
import code.config.GlobalActorSystem

object CometAlertController extends Loggable {
  implicit val timeout = Timeout(5 seconds)

  val hostAddress: String = java.net.InetAddress.getLocalHost.getHostAddress()

  /** These settings can be externalized  */
  val configString = """include "reference"
    akka.actor.provider = "akka.remote.RemoteActorRefProvider"
    akka.remote.netty.hostname = """" + hostAddress + """"
    akka.remote.netty.port     = 3557"""

  val configuration = ConfigFactory.parseString(configString)

  private def createActor = {
    logger.info("Creating Actor................." + hostAddress)
    val remoteSystem = ActorSystem("Node", ConfigFactory.load(configuration))
    val result = remoteSystem.actorOf(Props[AkkaAlertActor], "Manager")
    result
  }

  private def getOrCreateActor: ActorRef = {
    try {
      val actorRef = GlobalActorSystem.getActorSystem.actorFor("akka://Node@" + hostAddress + ":3557" + "/user/Manager")
      val future = akka.pattern.ask(actorRef, Ping())
      // Use Await.result, if blocking is necessary
      // Use mapTo for non-blocking scenario
      val result = Await.result(future, timeout.duration).asInstanceOf[Ping]
      actorRef
    } catch {
      case e: TimeoutException =>
        createActor
    }
  }

  def getManager: ActorRef = getOrCreateActor
}