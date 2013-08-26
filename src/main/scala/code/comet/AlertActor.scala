package code.comet

import scala.actors.Actor
import code.model.User
import net.liftweb.actor.LiftActor
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds
import net.liftweb.http.ActorWatcher
import akka.actor.ActorRef
import org.bson.types.ObjectId

/*class AlertActor extends Actor {

  var playerList: List[(U)] = Nil

  def act = {
    link(ActorWatcher)
    loop {
      react {
        case Subscribe(act, use: User) =>
          val user = new U(act, use)
          playerList = user :: playerList

        case Unsubscribe(act) =>
          playerList = playerList.filter(_.actor ne act)

        case Control(who, eml, msg) =>
          if (!eml.equals("")) {
            var friend = User.findByEmail(eml)
            playerList.foreach(a => if (a.user == friend.open_!) {
              a.actor ! Inside(msg)
            })
          }

        case _ => println("Manager - fallthru case")
      }
    }
  }
}
*/
class U(actorVar: ActorRef, id: ObjectId) {
  val actor = actorVar
  val userId = id
}
case class Subscribe(act: ActorRef, userId: ObjectId)
case class Unsubscribe(act: ActorRef)
case class Inside(msg: String)
case class Control(who: ActorRef, eml: String, msg: String)