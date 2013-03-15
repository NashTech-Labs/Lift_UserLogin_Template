package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._
import common._
import http._
import js.jquery.JQueryArtifacts
import sitemap._
import Loc._
import mapper._
import code.model._
import net.liftmodules.JQueryModule
import code.config._
import net.liftmodules.mongoauth.MongoAuth
import code.config.Site
import javax.mail._
import net.liftmodules.mongoauth.MongoAuth
import code.rest.UserRest
import code.rest.LoginRest
import code.rest.ReminderRest
import code.api.FacebookApiStateful
import code.service.GoogleDispatcher

/**
 * A class that's instantiated early and run.
 * It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    // appending dispatchers for rest services

    LiftRules.dispatch.append(LoginRest)
    LiftRules.dispatch.append(UserRest)
    LiftRules.dispatch.append(ReminderRest)
    LiftRules.dispatch.append(GoogleDispatcher.matcher)

   

    // init mongodb
    MongoConfig.init()

    // init auth-mongo
    MongoAuth.authUserMeta.default.set(User)
    MongoAuth.loginTokenAfterUrl.default.set(
      Site.password.url)
    MongoAuth.siteName.default.set("Knol")
    MongoAuth.systemEmail.default.set("test@test.com")
    MongoAuth.systemUsername.default.set("knol")

    /*
     * Init the jQuery module, 
     * see http://liftweb.net/jquery for more information.
     */
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery =
      JQueryModule.JQuery172
    JQueryModule.init()

    // For S.loggedIn_? and TestCond.loggedIn/Out builtin snippet
    LiftRules.loggedInTest = Full(
      () => User.isLoggedIn)

    // checks for ExtSession cookie
    LiftRules.earlyInStateful.append(
      User.testForExtSession)

    // Email sender
    System.setProperty(
      "mail.smtp.starttls.enable",
      "true");
    System.setProperty(
      "mail.smtp.host",
      "smtp.sendgrid.net")
    System.setProperty(
      "mail.smtp.auth",
      "true")
    def optionalAuth: Box[Authenticator] = {
      for {
        user <- Props.get("mail.smtp.user")
        pass <- Props.get("mail.smtp.pass")
      } yield new Authenticator {
        override def getPasswordAuthentication =
          new PasswordAuthentication(user, pass)
      }
    }

    Mailer.authenticator = optionalAuth

    // where to search snippet
    LiftRules.addToPackages("code")

    // set the default htmlProperties
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Build SiteMap
    LiftRules.setSiteMap(Site.siteMap)

    // 404 handler
    LiftRules.uriNotFound.prepend(
      NamedPF("404handler") {
        case (req, failure) =>
          NotFoundAsTemplate(
            ParsePath(
              List("404"),
              "html",
              false,
              false))
      })

    // Handle static files
    LiftRules.liftRequest.append {
      case Req("static" :: _, _, _) => false
    }

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show(
        "ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide(
        "ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(
      _.setCharacterEncoding("UTF-8"))

    LiftRules.dispatch.append(FacebookApiStateful)
    // Auto Fade Out notices
    LiftRules.noticesAutoFadeOut.default.
      set((noticeType: NoticeType.Value) =>
        Full((1 seconds, 2 seconds)))

    LiftRules.useXhtmlMimeType = false

  }
}
