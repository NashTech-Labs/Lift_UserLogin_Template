package code.comet

import scala.actors.Actor
import code.model.User
import net.liftweb.actor.LiftActor
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds
import net.liftweb.http.ActorWatcher

class AlertActor extends Actor {

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

class U(actorVar: LiftActor, use: User) {
  var actor = actorVar
  var user = use
}
case class Subscribe(act: LiftActor, user: User)
case class Unsubscribe(act: LiftActor)
case class Inside(msg: String)
case class Control(who: LiftActor, eml: String, msg: String)