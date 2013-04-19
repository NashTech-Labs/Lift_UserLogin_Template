package code.comet

import scala.actors.Actor
import Actor._
import net.liftweb._
import http._
import js._
import SHtml._
import JsCmds._
import common._
import util._
import Helpers._
import net.liftmodules.textile._
import _root_.scala.xml.{ NodeSeq, Text }
import _root_.java.util.Date
import net.liftweb.actor.LiftActor
import code.model.User
import code.model.Message
import net.liftweb.mapper.PreCache
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml
import akka.actor.ActorRef

case class Messages(msgs: List[String])

class Chat extends CometActor {

  private val user = User.currentUser match {
    case Full(usr) => usr
    case _ => User
  }
  private var chats: List[Message] = Message.findAll

  private val ulId = S.attr("ul_id") openOr "some_ul_id"

  private val liId = S.attr("li_id")

  private lazy val li = liId.
    flatMap { Helpers.findId(defaultXml, _) } openOr NodeSeq.Empty

  private val inputId = Helpers.nextFuncName

  private lazy val alertManager: ActorRef = CometAlertController.getManager

  override def render = {
    ("#" + ulId + " *") #> displayList
  }
  private def displayList: NodeSeq = chats.reverse.flatMap(line)

  private def line(m: Message) = {
    ("name=who" #> getName(m.user.obj.open_!.username.is) &
      "name=body" #> m.contentAsHtml)(li)
  }

  private def getName(name: String) = {
    if (user.username.is.equals(name)) {
      "me :-"
    } else {
      name+" :-"
    }
  }
  // appropriate dynamically generated code to the
  // view supplied by the template
  override lazy val fixedRender: Box[NodeSeq] =
    S.runTemplate("_chat_fixed" :: Nil,
      "postit" -> Helpers.evalElemWithId {
        (id, elem) =>
          SHtml.onSubmit((s: String) => {
            alertManager ! ChatServerMsg(user, s.trim)
            SetValById(id, "")
          })(elem)
      } _)

  override def localSetup {
    alertManager ! Subscribe(this, User.currentUser.open_!)
    super.localSetup
  }

  override def localShutdown {
    alertManager ! Unsubscribe(this)
    super.localShutdown
  }

  override def lowPriority = {
    case ChatServerUpdate(value) =>
      val update = (value diff chats).reverse.map(b => AppendHtml(ulId, line(b)))
      partialUpdate(update)
      chats = value
  }

}