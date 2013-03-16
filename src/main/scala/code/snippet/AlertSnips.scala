package code.snippet

import code.model.User
import scala.xml._
import scala.xml.{ NodeSeq, Text }
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import net.liftweb.http._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JE
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmds
import code.model.Reminder
import net.liftweb.mongodb.record.field.DateField
import java.text.SimpleDateFormat
import net.liftweb.http.js.JE.JsRaw
import com.mongodb.casbah.util.DeleteOp
import net.liftweb.http.PaginatorSnippet
import net.liftweb.mongodb.Limit
import code.model.User
import net.liftweb.json.JsonAST.JObject

class AlertSnips {
  val list = Reminder.getReminderForToday(User.currentUser.open_!.id.toString)

  def render = {
    "#alert" #> list.map { item =>
      "#listAlert" #> ("Today is " +item.friend_name +"'s birthday")
    }
  }
}