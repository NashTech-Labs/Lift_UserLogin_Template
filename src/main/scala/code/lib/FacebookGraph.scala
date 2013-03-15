package code
package lib

import org.joda.time.DateTime

import net.liftweb._
import common._
import json._
import http.{ Factory, S, SessionVar }
import util.{ Helpers, Props }
import dispatch._
import code.config._

import net.liftweb.util.Props

object FacebookGraph extends Factory with AppHelpers with Loggable {
  /*
   * Config
   */
  val key = new FactoryMaker[String](Props.get("facebook.key", "")) {}
  val secret = new FactoryMaker[String](Props.get("facebook.secret", "")) {}
  val callbackUrl = new FactoryMaker[String](Props.get("facebook.callbackurl", "/api/facebook/auth")) {}
  val channelUrl = new FactoryMaker[String](Props.get("facebook.channelurl", "/facebook/channel")) {}
  val permissions = new FactoryMaker[String](Props.get("facebook.permissions", "email,user_birthday")) {}

  private def baseReq = :/("graph.facebook.com").secure

  object currentAccessToken extends SessionVar[Box[AccessToken]](Empty)
  object currentFacebookId extends SessionVar[Box[Int]](Empty)

  /*
   * Do something with the current access token.
   */
  private def doWithToken[T](f: AccessToken => Box[T]): Box[T] = {
    currentAccessToken.is.flatMap { at =>
      if (at.isExpired) { // refresh the token
        val newToken = accessToken(at.code)
        currentAccessToken(newToken)
        newToken.flatMap(t => f(t))
      } else
        f(at)
    }
  }

  // where to send the user after connecting with facebook
  object continueUrl extends SessionVar[String](Site.home.url)

  // CSRF token
  object csrf extends SessionVar[String](Helpers.nextFuncName)

  // url that sends user to facebook to authorize the app
  def authUrl = "http://www.facebook.com/dialog/oauth?client_id=%s&redirect_uri=%s&scope=%s&state=%s&display=popup"
    .format(key.vend, Helpers.urlEncode(S.hostAndPath + callbackUrl.vend), permissions.vend, csrf.is)

  /*
   * Make a request and process the output with the given function
   */
  private[lib] def doRequest[T](req: Request)(func: String => Box[T]): Box[T] =
    /*
     * See: http://dispatch.databinder.net/Choose+an+Executor.html
     */
    Http x (req as_str) {
      case (400, _, _, out) => Failure(parseError(out()))
      case (200, _, _, out) =>
        val o = out()
        logger.debug("output from facebook: " + o)
        func(o)
      case (status, b, c, out) =>
        //logger.debug("b: "+b.toString)
        //logger.debug("c: "+c.toString)
        Failure("Unexpected status code: %s - %s".format(status, out()))
    }

  private def parseError(in: String): String = Helpers.tryo {
    val jsn = JsonParser.parse(in)
    val JString(errMsg) = jsn \\ "message"
    errMsg
  } openOr "Error parsing error: " + in

  /*
   * Make a request and parse the output to json
   */
  private[lib] def doReq(req: Request): Box[JValue] =
    doRequest(req) { out => Full(JsonParser.parse(out)) }

  /*
   * Make a request with the access token as a parameter
   */
  private def doOauthReq(req: Request, token: AccessToken): Box[JValue] = {
    val params = Map("access_token" -> token.value)
    doReq(req <<? params)
  }

  /*
   * Request an access token from facebook
   */
  def accessToken(code: String): Box[AccessToken] = {
    val req = baseReq / "oauth" / "access_token" <<? Map(
      "client_id" -> key.vend,
      "client_secret" -> secret.vend,
      "redirect_uri" -> (S.hostAndPath + callbackUrl.vend),
      "code" -> code)

    doRequest(req) { out =>
      val map = Map.empty ++ out.split("&").map { param =>
        val pair = param.split("=")
        (pair(0), pair(1))
      }

      (map.get("access_token"), map.get("expires")) match {
        case (Some(at), Some(exp)) => Helpers.asInt(exp)
          .map(e => AccessToken(at, code, (new DateTime).plusSeconds(e)))
        case _ => Failure("Unable to parse access_token: " + map.toString)
      }
    }
  }

  def me(token: AccessToken): Box[JValue] = doOauthReq(baseReq / "me", token)

  def me(token: AccessToken, obj: String): Box[JValue] =
    doOauthReq(baseReq / "me" / obj, token)

  def me: Box[JValue] = doWithToken {
    token => me(token)
  }

  def me(obj: String): Box[JValue] = doWithToken {
    token => me(token, obj)
  }

  //def obj(id: String): Box[JValue] = doReq(baseReq / id)

  def deletePermission(facebookId: Int, perm: Box[String] = Empty): Box[Boolean] = doWithToken {
    token =>
      val req = baseReq.DELETE / facebookId.toString / "permissions" <<?
        Map("access_token" -> token.value) ++ perm.map(p => ("permission" -> p)).toList <:<
        Map("Content-Length" -> "0") // http://facebook.stackoverflow.com/questions/4933780/why-am-i-getting-a-method-not-implemented-error-when-attempting-to-delete-a-fa

      doRequest(req) { out =>
        out match {
          case "true" => Full(true)
          case _ => Empty
        }
      }
  }

  /*
   * http://forum.developers.facebook.net/viewtopic.php?pid=344787
   */
  def parseSignedRequest(in: String): Box[JValue] = {
    import java.util.Arrays
    import javax.crypto.Mac
    import javax.crypto.spec.SecretKeySpec
    import org.apache.commons.codec.binary.Base64

    import Helpers.tryo

    in.split("""\.""").toList match {
      case sig :: payload :: Nil =>
        // decode the data
        val base64 = new Base64(true) // url friendly
        val sentSig = base64.decode(sig.replaceAll("-", "+").replaceAll("_", "/").getBytes)
        (for {
          json <- tryo(JsonParser.parse(new String(base64.decode(payload))))
          algo <- extractAlgo(json)
          ok <- boolToBox(algo.toUpperCase == "HMAC-SHA256") ?~ "Unknown algorithm. Expected HMAC-SHA256"
        } yield {
          logger.debug("signed request json: " + pretty(render(json)))
          val mac = Mac.getInstance("HmacSHA256")
          mac.init(new SecretKeySpec(secret.vend.getBytes, "HmacSHA256"))
          (mac.doFinal(payload.getBytes), json)
        }) match {
          case Full((expectedSig, json)) =>
            if (Arrays.equals(expectedSig, sentSig)) Full(json)
            else {
              logger.debug("expectedSig: " + expectedSig.toString)
              logger.debug("sentSig: " + sentSig.toString)
              Failure("Bad Signed JSON signature!")
            }
          case Empty => Empty
          case f: Failure => f
        }
      case x => Failure("Couldn't split input: " + x.toString)
    }
  }

  private def extractAlgo(jv: JValue): Box[String] = Helpers.tryo {
    val JString(algo) = jv \\ "algorithm"
    algo
  }
}

case class AccessToken(val value: String, val code: String, val expires: DateTime = (new DateTime).plusSeconds(600)) {
  def isExpired: Boolean = expires.isBefore(new DateTime)
}
