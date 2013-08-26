package code.lib

import akka.actor.Actor
import akka.actor.ActorRef
import net.liftweb.common.Loggable
import net.liftweb.http.CometActor
import code.config.GlobalActorSystem

/**
 * Because Lift has its own actor model, we need to
 * provide a bridge between Lift and our Akka game
 * engine actor. Basically this will forward messages
 * to a given CometActor once that CometActor has been
 * set.
 */
class BridgeActor extends Actor {
  private var target: Option[CometActor] = None
  def receive = {
    case comet: CometActor => target = Some(comet)
    case msg => target.foreach(_ ! msg)
  }
}

/**
 * Controls the life-cycle of Actor Bridges
 */
object BridgeController extends Loggable {

  def getBridgeActor: ActorRef = {
    GlobalActorSystem.getActorSystem.actorOf(akka.actor.Props[BridgeActor])
  }
}