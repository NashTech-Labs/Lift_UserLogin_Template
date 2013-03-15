package code.rest

/**
 * @author ayush
 */
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.S
import net.liftweb.common.Full
import code.service.LoginToken
import code.model.User
import net.liftweb.json.JsonDSL._

/**
 * This class is for exposing
 * Login Rest API
 */
object LoginRest extends RestHelper with ReminderUtil {

  serve {
    /**
     * To get login token after getting
     * @Param : email and password
     * curl  -X GET  http://localhost:8080/gettoken/b@b.com/123456 -H 'Content-type:application/json'
     */
    case "gettoken" :: email :: pass :: _ JsonGet _ ⇒
      authControlFunctionWithTwoParameters(
        responseJSON, email ::
          pass :: Nil,
        auth_not_req)
  }

  private def responseJSON(user: String, pass: String) = {
    val token = LoginToken.getToken(user, pass)
    token match {
      case Full(tkn) ⇒ {
        ("status", "200") ~
          ("access_token" -> tkn) ~
          ("token_type" -> "bearer") ~
          ("expires_in" -> "3600") ~ Nil
      }
      case _ ⇒ TOKEN_NOT_FOUND_JSON
    }
  }
}