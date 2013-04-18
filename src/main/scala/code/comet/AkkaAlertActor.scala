package code.comet

import akka.actor.Actor
import code.model.User

class AkkaAlertActor extends Actor {

  var playerList: List[(U)] = Nil

  def receive = {
    case Subscribe(act, use: User) =>
      val user = new U(act, use)
      playerList = user :: playerList

    case Unsubscribe(act) =>
      playerList = playerList.filter(_.actor ne act)

    case Control(who, eml, msg) =>
      println("control called")
      if (!eml.equals("")) {
        var friend = User.findByEmail(eml)
        playerList.foreach(a => if (a.user == friend.open_!) {
          a.actor ! Inside(msg)
        })
      }

    case _ => println("Manager - fallthru case")
  }

}