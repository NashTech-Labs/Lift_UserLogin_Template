package code.rest

import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.S
import java.util.Locale
import dispatch.Http
import net.liftweb.common.Full
import net.liftweb.common.Logger
import code.service.LoginToken

/**
 * Provides authentication gateway for functionality exposed through REST
 */
trait ReminderUtil extends Logger {

  val SERVER_ERROR_JSON = ("status" -> "500") ~ ("message" -> "Could not complete the request at this time") ~ Nil
  val STANDARD_SUCCESS_JSON = ("status" -> "200") ~ Nil
  val UNRECOGNIZED_ITEM_JSON = ("status" -> "411") ~ ("message" -> "This item could not be recognized.") ~ Nil
  val STANDARD_UNSUCCESS_JSON = ("status" -> "400") ~ Nil
  
  val auth_req = true
  val auth_not_req = false
  val scope_all = 0
  val scope_user_only = 1

  val TOKEN_EXPIRED_JSON = ("status" -> "401") ~ ("message" -> "This oauth_token has been expired") ~ Nil
  val TOKEN_INVALID_JSON = ("status" -> "402") ~ ("message" -> "oauth_token is not valid") ~ Nil
  val TOKEN_NOT_FOUND_JSON = ("status" -> "403") ~ ("message" -> "oauth_token is not found") ~ Nil
  val ITEM_NOT_FOUND = ("status" -> "404") ~ ("message" -> "Item Not Found") ~ Nil
  val WRONG_PASSWORD = ("status" -> "405") ~ ("message" -> "Wrong Password") ~ Nil
  val USER_EXIST = ("status" -> "409") ~ ("message" -> "User already exists !") ~ Nil
  val REMINDER_ALREADY_COMPLETED = ("status" -> "406")  ~ Nil
  val REMINDER_NOT_EXIST = ("status" -> "407") ~ ("message" -> "Reminder not exists !") ~ Nil
  def authControl(jsonObj: JObject, isAuth: Boolean): JObject =
    {
      var json: JObject = Nil
      if (isAuth == auth_not_req)
        json = jsonObj
      else if (isAuth == auth_req) {
        if (S.param("oauth_token").isDefined) {
          if (isValid(S.param("oauth_token").getOrElse(""))) {
            if (!isExpired(S.param("oauth_token").getOrElse("")))
              json = jsonObj
            else
              json = TOKEN_EXPIRED_JSON
          } else
            json = TOKEN_INVALID_JSON
        } else
          json = TOKEN_NOT_FOUND_JSON
      }
      json
    }

  /**
   * Provides authentication control for functions with one parameter
   */
  def authControlFunctionWithOneParamter(f: (String) ⇒ JObject, para: List[String], isAuth: Boolean): JObject =
    {
      var json: JObject = Nil
      if (isAuth == auth_not_req)
        json = f(para(0))
      else if (isAuth == auth_req) {
        if (S.param("oauth_token").isDefined) {
          if (isValid(S.param("oauth_token").getOrElse(""))) {
            if (!isExpired(S.param("oauth_token").getOrElse("")))
              json = f(para(0))
            else
              json = TOKEN_EXPIRED_JSON
          } else
            json = TOKEN_INVALID_JSON
        } else
          json = TOKEN_NOT_FOUND_JSON
      }
      json
    }

  /**
   * Provides authentication control for functions with two parameter
   */
  def authControlFunctionWithTwoParameters(f: (String, String) ⇒ JObject, para: List[String], isAuth: Boolean): JObject =
    {
      var json: JObject = Nil
      if (isAuth == auth_not_req)
        json = f(para(0), para(1))
      else if (isAuth == auth_req) {
        if (S.param("oauth_token").isDefined) {
          if (isValid(S.param("oauth_token").getOrElse(""))) {
            if (!isExpired(S.param("oauth_token").getOrElse("")))
              json = f(para(0), para(1))
            else
              json = TOKEN_EXPIRED_JSON
          } else
            json = TOKEN_INVALID_JSON
        } else
          json = TOKEN_NOT_FOUND_JSON
      }
      json
    }

  /**
   * Provides authentication control for functions with three parameters
   */
  def authControlFunctionWithThreeParameters(f: (String, String, String) ⇒ JObject, para: List[String], isAuth: Boolean): JObject =
    {
      var json: JObject = Nil
      if (isAuth == auth_not_req)
        json = f(para(0), para(1), para(2))
      else if (isAuth == auth_req) {
        if (S.param("oauth_token").isDefined) {
          if (isValid(S.param("oauth_token").getOrElse(""))) {
            if (!isExpired(S.param("oauth_token").getOrElse("")))
              json = f(para(0), para(1), para(2))
            else
              json = TOKEN_EXPIRED_JSON
          } else
            json = TOKEN_INVALID_JSON
        } else
          json = TOKEN_NOT_FOUND_JSON
      }
      json
    }

  /**
   * Provides authentication control for functions with four parameters
   */
  def authControlFunctionWithFourParameters(f: (String, String, String, String) ⇒ JObject, para: List[String], isAuth: Boolean): JObject =
    {
      var json: JObject = Nil
      if (isAuth == auth_not_req)
        json = f(para(0), para(1), para(2), para(3))
      else if (isAuth == auth_req) {
        if (S.param("oauth_token").isDefined) {
          if (isValid(S.param("oauth_token").getOrElse(""))) {
            if (!isExpired(S.param("oauth_token").getOrElse("")))
              json = f(para(0), para(1), para(2), para(3))
            else
              json = TOKEN_EXPIRED_JSON
          } else
            json = TOKEN_INVALID_JSON
        } else
          json = TOKEN_NOT_FOUND_JSON
      }
      json
    }

  def isValid(token: String): Boolean =
    {
      if (LoginToken.getID(token).equals("")) false
      else true
    }

  def isExpired(token: String): Boolean =
    {
      if (token.equals("eee")) true
      else false
    }

}
