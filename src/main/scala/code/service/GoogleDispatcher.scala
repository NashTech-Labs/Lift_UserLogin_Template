package code.service

import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.GetRequest
import net.liftweb.http.S
import net.liftweb.common.Box
import net.liftweb.http.LiftResponse
import net.liftweb.util.Props
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.http.SessionVar
import code.model.User
import net.liftweb.common.Loggable
import com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl
import java.util.Arrays
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.services.oauth2.Oauth2
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource
import com.google.api.services.oauth2.model.Userinfo
import org.bson.types.ObjectId

/**
 * Handles user login via Google.
 */
object GoogleDispatcher extends Loggable {

  /**
   * Dispatch requests for twitter. We are interested in
   * twitter/authenticate, twitter/callback and twitter/logout
   */
  def matcher: LiftRules.DispatchPF = {
    case req @ Req("google" :: "authenticate" :: Nil, _, GetRequest) =>
      () => signUpRedirect(req)
    case req @ Req("google" :: "catchtoken" :: Nil, _, GetRequest) =>
      () => processCallBack(req)
  }

  /**
   * Calls Google API to authenticate the user.
   * Post authentication Google would send the token back on callbackURL
   * @See OAuth2 with Google https://developers.google.com/accounts/docs/OAuth2UserAgent
   */
  def signUpRedirect(req: Req): Box[LiftResponse] = {
    val callbackURL = req.hostAndPath + "/google/callback"
    val url = new GoogleBrowserClientRequestUrl(Props.get("google.client.id").openOr(""),
      callbackURL, Arrays.asList("https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/userinfo.profile")).
      setState("/").build()
    S.redirectTo(url);

  }

  /**
   * Call back from Google post authentication.
   * Login the authenticated user or 'create and login' new user.
   */
  def processCallBack(req: Req): Box[LiftResponse] = {

    // fetch user info object form Google
    val userInfo = validateTokenAndFetchUser(req)

    // process the obtained user information
    createOrLoginUser(userInfo)

    S.redirectTo("/")
  }

  /**
   * Using Google client libraries to fetch the information.
   * @See http://stackoverflow.com/questions/11328832/how-to-validate-google-oauth2-token-from-java-code
   */
  private def validateTokenAndFetchUser(req: Req) = {
    val transport = new NetHttpTransport()
    val jsonFactory = new JacksonFactory()
    val accessToken = req.param("access_token").open_!
    val refreshToken = req.param("access_token").open_!

    // TODO GoogleAccessProtectedResource is marked as deprecated, need to check the alternate
    val requestInitializer = new GoogleAccessProtectedResource(accessToken, transport, jsonFactory,
      Props.get("google.client.id").openOr(""), Props.get("google.client.secret").openOr(""), refreshToken)

    // set up global Oauth2 instance
    val oauth2 = new Oauth2.Builder(transport, jsonFactory, requestInitializer).setApplicationName("Rishi").build()

    oauth2.userinfo().get().execute()

  }

  /**
   * If the user exists then login else create user.
   */
  private def createOrLoginUser(userInfo: Userinfo) = {
    User.findByEmail(userInfo.getEmail) match {
      case Full(user) => // needs merging
        User.logUserIn(user, true, true)

      case _ => { // new user; send to register page with form pre-filled
        val user = User.saveUser(userInfo.getEmail, "", userInfo.getName(), userInfo.getEmail)
        User.logUserIn(user, true, true)
      }
    }
  }

}