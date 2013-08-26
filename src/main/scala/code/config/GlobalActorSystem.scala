package code.config

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

/**
 * To create Single Actor System thought out the application
 */
object GlobalActorSystem {

  val localconfigString = """include "reference""""
  val system = ActorSystem("manager", ConfigFactory.load(ConfigFactory.parseString(localconfigString)))
  def getActorSystem = {
    system
  }
}