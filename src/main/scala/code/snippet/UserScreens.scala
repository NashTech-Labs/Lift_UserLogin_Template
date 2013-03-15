package code
package snippet

/**
 * This package provides screens for logged in users
 */
import config.Site
import lib.Gravatar
import model._

import scala.xml._

import net.liftweb._
import common._
import http.{ LiftScreen, S }
import util.FieldError
import util.Helpers._

/**
 * Use for editing the currently logged in user only.
 */
sealed trait BaseCurrentUserScreen extends BaseScreen {
  object userVar extends ScreenVar(
    User.currentUser.openOrThrowException(
      "User is currently not logged in . "))

  override def localSetup {
    Referer(Site.account.url)
  }
}

/**
 * Account screeen : For updating User's account setting
 */
object AccountScreen extends BaseCurrentUserScreen {
  addFields(() => userVar.is.accountScreenFields)

  def finish() {
    userVar.is.save
    S.notice("Account settings saved")
  }
}

sealed trait BasePasswordScreen {
  this: LiftScreen =>
  val passwordField = password(
    pwdName,
    "",
    trim,
    valMinLen(pwdMinLength, "Password must be at least " + pwdMinLength + " characters"),
    valMaxLen(pwdMaxLength, "Password must be " + pwdMaxLength + " characters or less"),
    ("tabindex" -> "1"))
  val confirmPasswordField = password("Confirm Password", "", trim, ("tabindex" -> "1"))

  def pwdName: String = S.?("password")
  def pwdMinLength: Int = 6
  def pwdMaxLength: Int = 32

  def passwordsMustMatch(): Errors = {
    if (passwordField.is != confirmPasswordField.is)
      List(FieldError(confirmPasswordField, "Passwords must match"))
    else Nil
  }
}

/**
 * Password Screen
 */
object PasswordScreen extends BaseCurrentUserScreen with BasePasswordScreen {
  override def pwdName = "New Password"
  override def validations = passwordsMustMatch _ :: super.validations

  def finish() {
    userVar.is.password(passwordField.is)
    userVar.is.password.hashIt
    userVar.is.save
    S.notice("New password saved")
  }
}

/**
 * Use for editing the currently logged in user only.
 */
object ProfileScreen extends BaseCurrentUserScreen {
  def gravatarHtml =
    User.currentUser match {
      case Full(user) => {
        <span>
          <div class="gravatar">
            { Gravatar.imgTag(user.email.is, 100) }
          </div>
          <div class="gravatar">
            <h4>
              Change your avatar at
              <a href="http://gravatar.com" target="_blank">Gravatar.com</a>
            </h4>
            <p>
              We're using{ user.email.is }
              . It may take time for changes made on gravatar.com to appear on our site.
            </p>
          </div>
        </span>
      }
      case _ if (S.request.flatMap(_.location).map(_.name).filterNot(
        it => List("Login", "Register").contains(it)).isDefined) =>
        <form action="/login" style="float: right">
          <button class="btn">Sign In</button>
        </form>
      case _ => NodeSeq.Empty
    }

  val gravatar = displayOnly("Picture", gravatarHtml)

  addFields(() => userVar.is.profileScreenFields)

  def finish() {
    userVar.is.save
    S.notice("Profile settings saved")
    S.redirectTo("/user/" + (userVar.username.is.replace(".", "%2E")))
  }

}

/**
 * this is needed to keep these fields
 *  and the password fields in the proper order
 */
trait BaseRegisterScreen extends BaseScreen {
  object userVar extends ScreenVar(User.createUserFromCredentials)

  addFields(() => userVar.is.registerScreenFields)
}

/**
 * Use for creating a new user.
 */
object RegisterScreen extends BaseRegisterScreen with BasePasswordScreen {
  override def validations = passwordsMustMatch _ :: super.validations

  val rememberMe = builder("", User.loginCredentials.is.isRememberMe, ("tabindex" -> "1"))
    .help(Text("Remember me when I come back later."))
    .make

  override def localSetup {
    Referer(Site.home.url)
  }

  def finish() {
    val user = userVar.is
    user.password(passwordField.is)
    user.password.hashIt
    user.save
    User.logUserIn(user, true)
    if (rememberMe) User.createExtSession(user.id.is)
    S.notice("Thanks for signing up!")
  }
}

