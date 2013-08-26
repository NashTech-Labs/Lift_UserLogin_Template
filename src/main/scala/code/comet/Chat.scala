package code.comet

import net.liftweb._
import http._
import js._
import SHtml._
import JsCmds._
import common._
import util._
import Helpers._
import _root_.scala.xml.{ NodeSeq, Text }
import _root_.java.util.Date
import code.model.User
import code.model.Message
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml
import akka.actor.ActorRef
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.util.Timeout
import scala.concurrent.Await
import code.lib.BridgeController
import org.bson.types.ObjectId
import net.liftmodules.textile.TextileParser

case class Messages(msgs: List[String])
case class Ping()

class Chat extends CometActor with Loggable {

  implicit val timeout = Timeout(5 seconds)
  private val curr_user_id = User.currentUser match {
    case Full(usr) => usr.id.is
    case _ => new ObjectId
  }
  private var chats: List[(ObjectId, String)] = Nil

  private val ulId = S.attr("ul_id") openOr "some_ul_id"

  private val liId = S.attr("li_id")

  private lazy val li = liId.
    flatMap { Helpers.findId(defaultXml, _) } openOr NodeSeq.Empty

  private val inputId = Helpers.nextFuncName

  private lazy val alertManager: ActorRef = CometAlertController.getManager

  private lazy val bridge: ActorRef = BridgeController.getBridgeActor

  override def render = {
    ("#" + ulId + " *") #> displayList
  }
  private def displayList: NodeSeq = chats.reverse.flatMap(line)

  private def line(tuple: (ObjectId, String)) = {
    ("name=who" #> getName(User.findByStringId(tuple._1.toString()).open_!.username.is) &
      "name=body" #> contentAsHtml(tuple._2))(li)
  }

  def contentAsHtml(content: String) = TextileParser.paraFixer(TextileParser.toHtml(content, Empty))

  private def getName(name: String) = {
    if (User.findByStringId(curr_user_id.toString()).open_!.username.is.equals(name)) {
      "me :-"
    } else {
      name + " :-"
    }
  }
  // appropriate dynamically generated code to the
  // view supplied by the template
  override lazy val fixedRender: Box[NodeSeq] =
    S.runTemplate("_chat_fixed" :: Nil,
      "postit" -> Helpers.evalElemWithId {
        (id, elem) =>
          SHtml.onSubmit((s: String) => {
            alertManager ! ChatServerMsg(curr_user_id, s.trim)
            SetValById(id, "")
          })(elem)
      } _)

  override def localSetup {
    bridge ! this
    val future = akka.pattern.ask(alertManager, Subscribe(bridge, User.currentUser.open_!.id.is))
    val result = Await.result(future, timeout.duration).asInstanceOf[List[(ObjectId, String)]]
    chats = result
    super.localSetup
  }

  override def localShutdown {
    alertManager ! Unsubscribe(bridge)
    bridge ! akka.actor.PoisonPill
    super.localShutdown
  }

  override def lowPriority = {
    case ChatServerUpdate(value) =>
      val update = (value diff chats).reverse.map(b => AppendHtml(ulId, line(b)))
      partialUpdate(update)
      chats = value
  }

}