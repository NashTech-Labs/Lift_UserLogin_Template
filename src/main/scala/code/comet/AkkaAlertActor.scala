package code.comet

import akka.actor.Actor
import code.model.User
import code.model.Message
import scala.xml.NodeSeq
import net.liftmodules.textile.TextileParser
import net.liftweb.common.Empty
import net.liftweb.util.TimeHelpers._
import java.util.Date

class AkkaAlertActor extends Actor {
  private var chats: List[Message] = Nil
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

    case ChatServerMsg(user, msg) if msg.length > 0 =>
      println("control called")
      val message = Message.createRecord
      message.user(user.id.is)
      if(msg == "kill") {
        throw new Exception
      }
      message.content((msg))
      message.createdAt(now)
      message.save
      chats ::= message
      chats = chats.take(50)
      playerList.foreach(a => a.actor ! ChatServerUpdate(chats.take(15)))

    case _ => println("Manager - fallthru case")
  }

  def createUpdate = ChatServerUpdate(chats.take(15))

  def toHtml(msg: String): NodeSeq = TextileParser.paraFixer(TextileParser.toHtml(msg, Empty))

}

case class ChatServerMsg(user: User,  msg: String)
case class ChatServerUpdate(msgs: List[Message])

case class ChatLine(user: String, msg: NodeSeq, when: Date)
