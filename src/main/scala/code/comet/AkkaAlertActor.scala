package code.comet

import akka.actor.Actor
import code.model.User
import code.model.Message
import scala.xml.NodeSeq
import net.liftmodules.textile.TextileParser
import net.liftweb.common.Empty
import net.liftweb.util.TimeHelpers._
import java.util.Date
import org.bson.types.ObjectId

class AkkaAlertActor extends Actor {
  private var chats: List[(ObjectId, String)] = Nil
  var playerList: List[(U)] = Nil

  def receive = {
    case Subscribe(act, userId: ObjectId) =>
      val user = new U(act, userId)
      playerList = user :: playerList
      val messages = for (msg <- Message.findAll) yield (msg.user.is, msg.content.is)
      sender ! messages

    case Unsubscribe(act) =>
      playerList = playerList.filter(_.actor ne act)

    case Control(who, eml, msg) =>
      if (!eml.equals("")) {
        var friend = User.findByEmail(eml)
        playerList.foreach(a => if (a.userId == friend.open_!.id.is) {
          a.actor ! Inside(msg)
        })
      }

    case ChatServerMsg(userId, msg) if msg.length > 0 =>
      println("control called")
      val message = Message.createRecord
      message.user(userId)
      if (msg == "kill") {
        throw new Exception
      }
      message.content((msg))
      message.createdAt(now)
      message.save
      chats ::= (userId, msg)
      chats = chats.take(50)
      playerList.foreach(a => a.actor ! ChatServerUpdate(chats.take(15)))

    case Ping() => sender ! Ping()

    case _ => println("Manager - fallthru case")
  }

}

case class ChatServerMsg(userId: ObjectId, msg: String)
case class ChatServerUpdate(msgs: List[(ObjectId, String)])

case class ChatLine(user: String, msg: NodeSeq, when: Date)
