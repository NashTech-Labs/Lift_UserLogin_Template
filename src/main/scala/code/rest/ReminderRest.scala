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
     * REST api for user to create Things To Do
     * @Param : Things To Do description , Due date
     * curl  -X POST 'http://localhost:8080/user/thingstodo/create?oauth_token=31363236323137393739&desc=Meeting+with+client&due_date=03-08-2013'
     */
    case "user" :: "thingstodo" :: "create" :: _ Post postData ⇒
      authControlFunctionWithThreeParameters(createThingsToDo,
        LoginToken.getID(S.param("oauth_token").getOrElse("")) ::
          S.param("desc").getOrElse("") :: S.param("due_date").getOrElse("") ::
          Nil,
        auth_req)

     /**
     * REST api for user to update Things To Do
     * @Param : Things To Do id, Things To Do description , Due date
     * curl  -X POST 'http://localhost:8080/user/thingstodo/update?oauth_token=31363236323137393739&id=513ebbe7e8e009d89b469d2e&desc=appointment+with+doctot&due_date=03-08-2013'
     */

    case "user" :: "thingstodo" :: "update" :: _ Post postData ⇒
      authControlFunctionWithFourParameters(updateThingsToDo,
        LoginToken.getID(S.param("oauth_token").getOrElse("")) ::
          S.param("id").getOrElse("") :: S.param("desc").getOrElse("") ::
          S.param("due_date").getOrElse("") :: Nil,
        auth_req)

    /**
     * REST api for user to delete Things To Do
     * @Param : Things To Do id
     * curl  -X POST 'http://localhost:8080/user/thingstodo/delete?oauth_token=31363236323137393739&id=513ebbe7e8e009d89b469d2e'
     */

    case "user" :: "thingstodo" :: "delete" :: _ Post postData ⇒
      authControlFunctionWithTwoParameters(deleteThingsToDo,
        LoginToken.getID(S.param("oauth_token").getOrElse("")) ::
          S.param("id").getOrElse("") :: Nil,
        auth_req)

  }

  /**
   * Create Things to Do
   */

  private def createThingsToDo(user_id: String, desc: String, due_date: String) = {
    try {
      var result = Reminder.createThingsToDo(user_id, desc, due_date.replace("-", "/"))
      result match {
        case Right(status) => STANDARD_SUCCESS_JSON ~ ("message" -> "Things To Do created successfully !")
        case Left(notice) => STANDARD_UNSUCCESS_JSON ~ ("message" -> (notice))
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

 
  /**
   * Update Things to Do
   */

  private def updateThingsToDo(user_id: String, thingsToDo_id: String, desc: String, due_date: String) = {
    try {
      Reminder.find(thingsToDo_id) match {
        case Empty => THINGSTODO_NOT_EXIST
        case Full(thingsToDo) => {
          var result = Reminder.updateThingsToDo(thingsToDo, desc, due_date.replace("-", "/"))
          result match {
            case Right(status) => STANDARD_SUCCESS_JSON ~ ("message" -> "Things To Do updated successfully !")
            case Left(notice) => STANDARD_UNSUCCESS_JSON ~ ("message" -> (notice))
          }
        }
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

  /**
   * Delete Things to Do
   */

  private def deleteThingsToDo(user_id: String, thingsToDo_id: String) = {
    try {
      Reminder.find(thingsToDo_id) match {
        case Empty => THINGSTODO_NOT_EXIST
        case Full(thingsToDo) => {
          Reminder.deleteThingsToDo(thingsToDo)
          STANDARD_SUCCESS_JSON ~ ("message" -> "Things To Do deleted successfully !")
        }
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

}