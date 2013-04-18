package code.comet

import net.liftweb.http.CometActor
import net.liftweb.common._
import net.liftweb.http.SHtml
import net.liftweb.http.CometActor
import net.liftweb.common._
import net.liftweb.http.SHtml
import net.liftweb.http.js._
import net.liftweb.util._
import Helpers._
import net.liftweb.http._
import net.liftweb.http._
import net.liftweb._
import mapper._
import S._
import SHtml._
import util._
import Helpers._
import js._
import JsCmds._
import JE._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import scala.xml._
import S._
import net.liftweb.util._
import JE._
import js.jquery._
import JqJsCmds._
import JqJE._
import java.util._
import code.comet._
import code.model.User
import akka.actor._

class CometAlert extends CometActor {
  override def defaultPrefix = Full("alert")
  private lazy val alertManager: ActorRef = CometAlertController.getManager

  def render = {
    "#alertSubmit [onClick]" #> SHtml.ajaxCall(JE.JsRaw("""$("#txtEmail").val() """), email => {
      var msg="You have been added by "+User.currentUser.open_!.name
      alertManager ! Control(this, email,msg)
    })
  }

  override def localSetup {
    alertManager ! Subscribe(this, User.currentUser.open_!)
    super.localSetup
  }

  override def localShutdown {
    alertManager ! Unsubscribe(this)
    super.localShutdown
  }

  override def lowPriority = {
    case Inside(msg) =>
      partialUpdate(SetHtml("msg", Text(msg)))
     // S.notice(msg)
  }
}
