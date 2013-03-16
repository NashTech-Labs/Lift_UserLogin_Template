package code.snippet
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

class Remindersnips extends SortedPaginatorSnippet[Reminder, Date] with PaginatorSnippet[Reminder] {

  var friend_name = ""
  var dob = ""
  var model_friend_name = ""
  var model_dob = ""

  def sortOrder = S.param("asc") match {
    case e: EmptyBox => 0
    case b: Box[String] => if (b.get.equalsIgnoreCase("true")) 1 else -1
  }

  override def count = Reminder.findAllNotes(
    User.currentUser.open_!.userIdAsString).size

  override def itemsPerPage = 5

  override def page = Reminder.findAllNotes(
    User.currentUser.open_!.userIdAsString,
    sortOrder,
    itemsPerPage,
    (curPage * itemsPerPage))

  def headers: List[(String, java.util.Date)] = List(("duedate", Reminder.dob.is.noTime))

  object refreshtable extends RequestVar(rendertodoField)
  object refreshPagination extends RequestVar(renderPagination)

  def render = {
    "#datepicker" #> SHtml.text(dob, dob = _) &
      "#bdayReminder" #> SHtml.text(friend_name, name => {
        friend_name = name
      }) &
      "#submit" #> SHtml.ajaxSubmit("Add", () => {
        Reminder.createReminder(User.currentUser.open_!.id.toString, friend_name, dob) match {
          case Left(notice) => S.error(notice)
          case Right(status) => {
            reloadTable &
              recallDatepicker &
              JsCmds.SetValById("datepicker", "") &
              JsCmds.SetValById("bdayReminder", "")
          }
        }
      }) &
      "#myTable" #> refreshtable.is &
      "#pagination" #> refreshPagination.is
  }

  private def renderPagination = SHtml.memoize {
    "#pagination" #> (<div>
                        <lift:remindersnips.paginate>
                          <p>
                            <nav:records></nav:records>
                          </p>
                          <nav:first></nav:first>
                          |<nav:prev></nav:prev>
                          |<nav:allpages></nav:allpages>
                          |<nav:next></nav:next>
                          |<nav:last></nav:last>
                        </lift:remindersnips.paginate>
                      </div>)
  }
  private def rendertodoField = SHtml.memoize {

    "#todoList1" #> page.map { item =>
      "#bdayReminderID" #> item.friend_name.is &
        "#dateID" #> Util.convertUtilDateToString(item.dob.is, "dd MMM yyyy") &
        ".btn_2 [id]" #> (item.id.toString + "_btn_1") &
        ".btn_3 [onclick]" #> SHtml.ajaxInvoke(() => {
          Reminder.deleteReminder(item)
          reloadTable &
            recallDatepicker
        }) &
        "#model [id]" #> (item.id.toString + "_model") &
        "#model_bdayReminder" #> SHtml.text(item.friend_name.is, name => {
          model_friend_name = name
          JsCmds.Noop
        }) &
        ".datepick" #> SHtml.text(Util.convertUtilDateToString(item.dob.is, "MM/dd/yyyy"), date => {
          model_dob = date
        }) &
        ".btn_2 [onclick]" #> showModelPopUp(item) &
        ".model_cancel [onclick]" #> hideModelPopUp(item.id.toString + "_model") &
        ".model_submit " #> SHtml.ajaxSubmit("Update", () => {
          Reminder.updateReminder(item, model_friend_name, model_dob) match {
            case Left(notice) => {
              S.error(notice)
              hideModelPopUp(item.id.toString + "_model")
            }
            case Right(status) => {
              S.notice("successfullt updated")
              hideModelPopUp(item.id.toString + "_model") &
                reloadTable
            }
          }
        })
    }
  }

  private def reloadTable: JsCmd = {
    SetHtml("myTable", refreshtable.is.applyAgain) &
      SetHtml("pagination", refreshPagination.is.applyAgain)
  }

  private def showModelPopUp(reminder: Reminder) = {
    JsRaw("""var modal= document.getElementById('""" + reminder.id.toString + """_model');
             var shade= document.getElementById('shade');
             modal.style.display= shade.style.display= 'block';""").cmd &
      recallDatepicker
  }

  private def hideModelPopUp(reminder: String) = {
    JsRaw("""var modal= document.getElementById('""" + reminder + """');
             var shade= document.getElementById('shade');
             modal.style.display=shade.style.display= 'none';""").cmd
  }

  private def recallDatepicker() = {
    JsRaw("""$(function() {
                    $('.datepick').datepick({alignment:"bottomRight"});
                    });""").cmd
  }

}