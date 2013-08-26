package code.lib

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.remote.RemoteActorRefProvider

/**
 * Utility for getting the port of the started actor system
 */
object NettyPort {
  def apply(system: ActorSystem): NettyPort = new NettyPort(system.asInstanceOf[ExtendedActorSystem])
}
class NettyPort(system: ExtendedActorSystem) {
  val port: Int = system.provider match {
    case r: RemoteActorRefProvider => r.transport.address.port.get.asInstanceOf[Int]
    case _: Exception => -1
  }
}
