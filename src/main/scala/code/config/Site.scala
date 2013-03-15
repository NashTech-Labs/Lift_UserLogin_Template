package code
package config

import model.User
import net.liftweb._
import common._
import http.S
import sitemap._
import sitemap.Loc._
import net.liftmodules.mongoauth.Locs
import net.liftweb.http.RedirectResponse
import net.liftweb.util.Helpers
import net.liftweb.util.Props
import code.lib.FacebookGraph

object MenuGroups {
  val AdminGroup = LocGroup("admin")
  val SettingsGroup = LocGroup("settings")
  val TopBarGroup = LocGroup("topbar")

}

/**
 * Wrapper for Menu locations
 */
case class MenuLoc(menu: Menu) {
  lazy val url = S.contextPath +
    menu.loc.calcDefaultHref
  lazy val fullUrl = S.hostAndPath +
    menu.loc.calcDefaultHref
}

/**
 * Create Menus with Navigator
 */
object Site extends Locs {
  import MenuGroups._

  /**
   * locations (menu entries)
   */
  val ConnectedToFacebook = If(
    () => User.isConnectedToFaceBook,
    () => RedirectResponse(home.url))

  val NotConnectedToFacebook = If(
    () => !User.isConnectedToFaceBook,
    () => RedirectResponse(home.url))

  private val profileParamMenu = Menu.param[User](
    "User", "Profile", User.findByUsername _, _.username.is) /
    "user" >> Loc.CalcValue(() => User.currentUser)

  lazy val profileLoc = profileParamMenu.toLoc

  /**
   * Non Logged in user will be
   * redirected to login page .
   */
  val loggedIn = If(() => (User.currentUser.isEmpty),
    () => RedirectResponse("/home"))

  val home = MenuLoc(Menu.i("Home") / "home" >> RequireLoggedIn)
  val index = MenuLoc(Menu.i("Index") / "index" >> loggedIn)
  val loginToken = MenuLoc(buildLoginTokenMenu)
  val logout = MenuLoc(buildLogoutMenu)
  val users = MenuLoc(Menu.i("Users") / "admin" / "users" >> AdminGroup >> RequireLoggedIn >> HasRole("admin"))
  val categories = MenuLoc(Menu.i("Categories") / "admin" / "categories" >> AdminGroup >> RequireLoggedIn >> HasRole("superuser"))
  val database = MenuLoc(Menu.i("Database") / "admin" / "database" >> AdminGroup >> RequireLoggedIn >> HasRole("superuser"))
  val password = MenuLoc(Menu.i("Password") / "settings" / "password" >> RequireLoggedIn)
  val reminder = MenuLoc(Menu.i("Reminder") / "reminder" >> RequireLoggedIn)
  val account = MenuLoc(Menu.i("Account") / "settings" / "account" >> SettingsGroup >> RequireLoggedIn)
  val editProfile = MenuLoc(Menu("EditProfile", "Profile") / "settings" / "profile" >> SettingsGroup >> RequireLoggedIn)
  val register = MenuLoc(Menu.i("Register") / "register" >> RequireNotLoggedIn)
  // streams
  val createStream = MenuLoc(
    Menu("CreateStream",
      "Create Stream") /
      "stream" /
      "create" >>
      RequireLoggedIn)
  val googleCallback = MenuLoc(Menu.i("GoogleCallback") / "google" / "callback" >> Hidden)
  val joinStream = MenuLoc(
    Menu("JoinStream",
      "Join Stream") /
      "stream" /
      "join" >>
      RequireLoggedIn)
  val facebookConnect = MenuLoc(
    Menu.i("FacebookConnect") / "facebook" / "connect" >> EarlyResponse(() => {
      FacebookGraph.csrf(Helpers.nextFuncName)
      val xx = RedirectResponse(FacebookGraph.authUrl, S.responseCookies: _*)
      Full(RedirectResponse(FacebookGraph.authUrl, S.responseCookies: _*))
      //Full(RedirectWithState(facebookError.url, RedirectState(() => { S.error("this is only a test") })))
    }))
  // popup page
  val facebookChannel = MenuLoc(Menu.i("FacebookChannel") / "facebook" / "channel" >> Hidden)
  val facebookClose = MenuLoc(Menu.i("FacebookClose") / "facebook" / "close" >> Hidden)
  val facebookError = MenuLoc(Menu("FacebookError", "Error") / "facebook" / "error" >> Hidden)
  val facebookRegister = MenuLoc(Menu("FacebookRegister", "Register") / "facebook" / "register" >> Hidden)
  override def RedirectToIndexWithCookies = {
    RedirectResponse(
      "/beta",
      S.responseCookies: _*)
  }

  private def menus = List(
    index.menu,
    home.menu,
    Menu.i("Category") /
      "category" >> Hidden,
    Menu.i("searchResults") /
      "searchResults" >> Hidden,
    Menu.i("Login") /
      "login" >> RequireNotLoggedIn,
    register.menu,
    loginToken.menu,
    logout.menu,
    profileParamMenu,
    account.menu,
    password.menu,
    editProfile.menu,
    users.menu,
    categories.menu,
    reminder.menu,
    database.menu,
    Menu.i("About") /
      "about" >> TopBarGroup,
    Menu.i("Setting") /
      "setting" >> Hidden,
    Menu.i("Throw") /
      "throw" >> Hidden,
    Menu.i("Error") /
      "error" >> Hidden,
    Menu.i("404") /
      "404" >> Hidden,
    // facebook related URLs 
    facebookConnect.menu,
    facebookChannel.menu,
    facebookClose.menu,
    facebookError.menu,
    facebookRegister.menu,
    googleCallback.menu)

  /**
   * Return a SiteMap needed for Lift
   */
  def siteMap = SiteMap(menus: _*)
}
