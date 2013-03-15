package code.rest

/**
 * @author ayush
 */
import net.liftweb.http.rest.RestHelper
import code.model.User
import code.service.LoginToken
import net.liftweb.http.S
import net.liftweb.json.JsonDSL._

/**
 * This class is for exposing
 * REST API for user
 */
object UserRest extends RestHelper with ReminderUtil {

  serve {

    /**
     * REST api to create user
     * @Param : email and password
     * curl  -X POST 'http://localhost:8080/user/create/a@a.com/123456'
     */
    case "user" :: "create" :: email :: pass :: _ Post postData ⇒
      authControlFunctionWithTwoParameters(createUser, email :: pass :: Nil, auth_not_req)

    /**
     * REST api for user to update password
     * @Param : old password , new password
     * curl  -X POST 'http://localhost:8080/user/updatePassword/123456/654321?oauth_token=2D313539313835313931'
     */
    case "user" :: "updatePassword" :: old_pass :: new_pass :: _ Post postData ⇒
      authControlFunctionWithThreeParameters(updatePassword,
        LoginToken.getID(S.param("oauth_token").getOrElse("")) ::
          old_pass :: new_pass ::
          Nil,
        auth_req)

  }

  /**
   *  Create User
   */

  private def createUser(email: String, password: String) = {
    try {
      val result = User.createUser(email, password)
      result match {
        case Left(status) => ("status" -> "400") ~ ("message" -> status)
        case Right(status) => {
          status match {
            case true => {
              STANDARD_SUCCESS_JSON ~ ("message" -> "User created successfully !")
            }
            case false => USER_EXIST
          }
        }
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

  /**
   * Update User Password
   */

  private def updatePassword(user_id: String, old_pass: String, new_pass: String) = {
    try {
      val user = User.find("_id" -> user_id)
      if (User.updatePassword(user.get, old_pass, new_pass)) {
        STANDARD_SUCCESS_JSON
      } else {
        WRONG_PASSWORD
      }
    } catch {
      case ex: Exception ⇒ SERVER_ERROR_JSON
    }
  }

}