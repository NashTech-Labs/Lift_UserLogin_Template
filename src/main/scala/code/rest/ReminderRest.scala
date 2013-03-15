package code.rest

import net.liftweb.http.rest.RestHelper
import code.model.User
import code.service.LoginToken
import net.liftweb.http.S
import net.liftweb.json.JsonDSL._
import code.model.Reminder
import net.liftweb.common.Empty
import net.liftweb.common.Full

object ReminderRest extends RestHelper with ReminderUtil {

  serve {

    /**
     * REST api for user to create Reminder
     * @Param : Friend's Name and Birthday
     * curl  -X POST 'http://localhost:8080/user/reminder/create?oauth_token=31363236323137393739&desc=test&birth_date=03-08-2013'
     */
    case "user" :: "reminder" :: "create" :: _ Post postData ⇒
      authControlFunctionWithThreeParameters(createReminder,
        LoginToken.getID(S.param("oauth_token").getOrElse("")) ::
          S.param("desc").getOrElse("") :: S.param("birth_date").getOrElse("") ::
          Nil,
        auth_req)

    /**
     * REST api for user to update Reminder
     * @Param : Reminder id, Friend's name , Birth date
     * curl  -X POST 'http://localhost:8080/user/reminder/update?oauth_token=31363236323137393739&id=513ebbe7e8e009d89b469d2e&desc=test&birth_date=03-08-2013'
     */

    case "user" :: "reminder" :: "update" :: _ Post postData ⇒
      authControlFunctionWithFourParameters(updateReminder,
        LoginToken.getID(S.param("oauth_token").getOrElse("")) ::
          S.param("id").getOrElse("") :: S.param("desc").getOrElse("") ::
          S.param("birth_date").getOrElse("") :: Nil,
        auth_req)

    /**
     * REST api for user to delete Reminder
     * @Param : Reminder id
     * curl  -X POST 'http://localhost:8080/user/reminder/delete?oauth_token=31363236323137393739&id=513ebbe7e8e009d89b469d2e'
     */

    case "user" :: "reminder" :: "delete" :: _ Post postData ⇒
      authControlFunctionWithTwoParameters(deleteReminder,
        LoginToken.getID(S.param("oauth_token").getOrElse("")) ::
          S.param("id").getOrElse("") :: Nil,
        auth_req)

  }

  /**
   * Create reminder
   */

  private def createReminder(user_id: String, desc: String, due_date: String) = {
    try {
      var result = Reminder.createReminder(user_id, desc, due_date.replace("-", "/"))
      result match {
        case Right(status) => STANDARD_SUCCESS_JSON ~ ("message" -> "Reminder created successfully !")
        case Left(notice) => STANDARD_UNSUCCESS_JSON ~ ("message" -> (notice))
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

  /**
   * Update Reminder
   */

  private def updateReminder(user_id: String, Reminder_id: String, desc: String, due_date: String) = {
    try {
      Reminder.find(Reminder_id) match {
        case Empty => REMINDER_NOT_EXIST
        case Full(reminder) => {
          var result = Reminder.updateReminder(reminder, desc, due_date.replace("-", "/"))
          result match {
            case Right(status) => STANDARD_SUCCESS_JSON ~ ("message" -> "Reminder updated successfully !")
            case Left(notice) => STANDARD_UNSUCCESS_JSON ~ ("message" -> (notice))
          }
        }
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

  /**
   * Delete Reminder
   */

  private def deleteReminder(user_id: String, reminder_id: String) = {
    try {
      Reminder.find(reminder_id) match {
        case Empty => REMINDER_NOT_EXIST
        case Full(reminder) => {
          Reminder.deleteReminder(reminder)
          STANDARD_SUCCESS_JSON ~ ("message" -> "Reminder deleted successfully !")
        }
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

}