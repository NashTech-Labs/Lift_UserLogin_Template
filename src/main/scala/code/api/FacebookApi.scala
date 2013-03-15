package code
package api

import lib.{ AccessToken, AppHelpers, FacebookGraph }
import model.User
import net.liftweb._
import common._
import http._
import http.rest.RestHelper
import json._
import util.Helpers._
import code.config.Site
import org.bson.types.ObjectId

object FacebookApiStateful extends RestHelper with AppHelpers with Loggable {
  def registerUrl = "%s?url=%s".format(Site.facebookClose.url, Site.register.url)
  def successUrl = Site.facebookClose.url
  def errorUrl = Site.facebookError.url
  def homeUrl = "%s?url=%s".format(Site.facebookClose.url, Site.home.url)

  serve("api" / "facebook" prefix {
    /*
     * This is the url that Facebook calls back to when authorizing a user
     */
    case "auth" :: Nil Get _ => {
      val redirectUrl: String =
        (S.param("code"), S.param("error"), S.param("error_reason"), S.param("error_description")) match {
          case (Full(code), _, _, _) =>
            (for {
              state <- S.param("state") ?~ "State not provided"
              ok <- boolToBox(state == FacebookGraph.csrf.is) ?~ "The state does not match. You may be a victim of CSRF."
              accessToken <- FacebookGraph.accessToken(code)
              json <- FacebookGraph.me(accessToken)
              facebookId <- extractId(json)
            } yield {
              logger.debug("auth json: " + pretty(render(json)))

              // set the access token session var
              FacebookGraph.currentAccessToken(Full(accessToken))

              User.findByFacebookId(facebookId) match {
                case Full(user) => validateUser(user) // already connected
                case _ =>
                  User.fromFacebookJson(json).map { facebookUser =>
                    User.findByEmail(facebookUser.email.is) match {
                      case Full(user) => // needs merging
                        validateUser(user)
                      case _ => // new user; send to register page with form pre-filled
                        val user = User.saveUser(facebookUser.email.is, facebookUser.username.is, facebookUser.name.is, facebookUser.username.is)
                        validateUser(user)
                    }
                  } openOr handleError("Error creating user from facebook json")
              }
            }) match {
              case Full(url) => url
              case Failure(msg, _, _) => handleError(msg)
              case Empty => handleError("Unknown error")
            }
          case (_, Full(error), Full(reason), Full(desc)) => // user denied authorization, ignore
            successUrl
          case _ => handleError("Unknown request type")
        }
      RedirectResponse(redirectUrl, S.responseCookies: _*)
    }

    /*
     * This is called by Facebook when a user deauthorizes this app on facebook.com
     */
    case "deauth" :: Nil Post _ => {
      (for {
        signedReq <- S.param("signed_request")
        json <- FacebookGraph.parseSignedRequest(signedReq)
        facebookId <- extractUserId(json)
        user <- User.findByFacebookId(facebookId)
      } yield {
        // deauthorize facebook
        User.disconnectFacebook(user)
      }) match {
        case Full(_) =>
        case Failure(msg, _, _) => handleError(msg)
        case Empty => handleError("Unknown error")
      }

      OkResponse()
    }

    /*
     * Call this via ajax when checking login status with JavaScript SDK.
     * Sets the access token and current facebookId.
     */
    case "init" :: Nil Post _ => boxJsonToJsonResponse {
      import JsonDSL._
      for {
        accessToken <- S.param("accessToken") ?~ "Token not provided"
        userId <- S.param("userID") ?~ "UserId not provided"
        facebookId <- asInt(userId) ?~ "Invalid Facebook user id"
        signedReq <- S.param("signedRequest") ?~ "Signed request not provided"
        expiresIn <- S.param("expiresIn") ?~ "ExpiresIn not provided"
        json <- FacebookGraph.parseSignedRequest(signedReq)
      } yield {
        val JString(code) = json \\ "code"
        logger.debug("expiresIn: " + expiresIn)
        // set the access token session var
        FacebookGraph.currentAccessToken(Full(AccessToken(accessToken, code)))
        // set the facebookId
        FacebookGraph.currentFacebookId(Full(facebookId))
        ("status" -> "ok")
      }
    }

    /*
     * Log in a user by their facebookId
     */
    case "login" :: Nil Post _ => boxJsonToJsonResponse {
      import JsonDSL._
      for {
        facebookId <- FacebookGraph.currentFacebookId.is ?~ "currentFacebookId not set"
        user <- User.findByFacebookId(facebookId) ?~ "User not found by facebookId"
      } yield {
        if (user.validate.length == 0) {
          User.logUserIn(user, true, true)
          ("url" -> User.loginContinueUrl.is)
        } else {
          User.regUser(user)
          ("url" -> Site.register.url)
        }
      }
    }
  })

  private def extractId(jv: JValue): Box[Int] = tryo {
    val JString(fbid) = jv \ "id"
    toInt(fbid)
  }

  private def extractUserId(jv: JValue): Box[Int] = tryo {
    val JString(fbid) = jv \ "user_id"
    toInt(fbid)
  }

  private def handleError(msg: String): String = {
    logger.error(msg)
    S.error(msg)
    errorUrl
  }

  private def validateUser(user: User): String = {
    user.validate match {
      case Nil => User.loginUser(user); successUrl
      case errs => User.regUser(user); registerUrl
    }
  }
}