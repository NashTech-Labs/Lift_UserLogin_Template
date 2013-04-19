package code
package snippet

import config.Site
import lib.{ Gravatar, AppHelpers }
import model.{ User, LoginCredentials }
import scala.xml._
import net.liftweb._
import common._
import http.{ DispatchSnippet, S, SHtml, StatefulSnippet }
import util._
import Helpers._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds

sealed trait UserSnippet extends DispatchSnippet with AppHelpers with Loggable {

  def dispatch = {
    case "header" => header
    case "gravatar" => gravatar
    case "name" => name
    case "username" => username
    case "title" => title
  }

  protected def user: Box[User]

  protected def serve(snip: User => NodeSeq): NodeSeq =
    (for {
      u <- user ?~ "User not found"
    } yield {
      snip(u)
    }): NodeSeq

  def header(xhtml: NodeSeq): NodeSeq = serve { user =>
    <div id="user-header">
      { gravatar(xhtml) }
      <h3>{ name(xhtml) }</h3>
    </div>
  }

  def gravatar(xhtml: NodeSeq): NodeSeq = {
    val size = S.attr("size").map(toInt) openOr Gravatar.defaultSize.vend

    serve { user =>
      Gravatar.imgTag(user.email.is, size)
    }
  }

  def username(xhtml: NodeSeq): NodeSeq = serve { user =>
    Text(user.username.is)
  }

  def name(xhtml: NodeSeq): NodeSeq = serve { user =>
    if (user.name.is.length > 0)
      Text("%s (%s)".format(user.name.is, user.username.is))
    else
      Text(user.username.is)
  }

  def title(xhtml: NodeSeq): NodeSeq = serve { user =>
    <lift:head>
      <title lift="Menu.title">{ "Welcome: %*% - " + user.username.is }</title>
    </lift:head>
  }
}

object CurrentUser extends UserSnippet {
  override protected def user = User.currentUser
}

object ProfileLocUser extends UserSnippet {
  override def dispatch = super.dispatch orElse {
    case "profile" => profile
  }

  override protected def user = Site.profileLoc.currentValue

  import java.text.SimpleDateFormat

  val df = new SimpleDateFormat("MMM d, yyyy")

  def profile(xhtml: NodeSeq): NodeSeq = serve { user =>
    val editLink: NodeSeq =
      if (User.currentUser.filter(_.id.is == user.id.is).isDefined)
        <a href={ Site.editProfile.url } class="btn info">Edit Your Profile</a>
      else
        NodeSeq.Empty

    val cssSel =
      "#id_avatar *" #> Gravatar.imgTag(user.email.is) &
        "#id_name *" #> <h3>{ user.name.is }</h3> &
        "#id_location *" #> user.location.is &
        "#id_whencreated" #> df.format(user.whenCreated.toDate).toString &
        "#id_bio *" #> user.bio.is &
        "#id_editlink *" #> editLink

    cssSel.apply(xhtml)
  }
}

class UserLogin extends StatefulSnippet with Loggable {
  def dispatch = { case "render" => render }

  // form vars
  private var password = ""
  private var hasPassword = false
  private var remember = User.loginCredentials.is.isRememberMe

  val radios = SHtml.radioElem[Boolean](
    Seq(false, true),
    Full(hasPassword))(it => it.foreach(hasPassword = _))

  def render = {
    "#id_email [value]" #> User.loginCredentials.is.email &
      "#id_password" #> SHtml.password(password, password = _) &
      "#no_password" #> radios(0) &
      "#yes_password" #> radios(1) &
      "name=remember" #> SHtml.checkbox(remember, remember = _) &
      "#signUp [onclick]" #> SHtml.onEvent(_ => S.seeOther(Site.register.url)) &
      "#id_submit [value]" #> "Login" &
      "#id_submit" #> SHtml.onSubmitUnit(process) &
      "#id_cancel" #> SHtml.onSubmitUnit(cancel)
  }

  private def process() {
    S.param("email").map(e => {
      User.checkPassowrd(e, hasPassword) match {
        case Left(notice) => {
          S.notice(notice)
          S.seeOther(Site.home.url)
        }
        case Right(status) => {
          status match {
            case false => S.seeOther(Site.register.url)
            case true => {
              var result = User.process(e, password, hasPassword, remember)
              result match {
                case Left(error) => S.error(error)
                case _ => S.seeOther(Site.home.url)
              }
            }
          }
        }
      }
    }) openOr S.error("Please enter an email address")
  }

  private def cancel() = S.seeOther(Site.home.url)
}
object AdminTopbar {
  def render = {
    //if (User.currentUser.hasRole("admin"))
    <ul class="nav nav-pills" id="admin">
      <li class="dropdown" data-dropdown="dropdown">
        <a href="#" data-toggle="dropdown" class="dropdown-toggle">Admin</a>
        <ul class="dropdown-menu">
          <li><lift:Menu.item name="Users" donthide="true" linktoself="true">
                Users
              </lift:Menu.item></li>
          <li><lift:Menu.item name="Categories" donthide="true" linktoself="true">
                Categories
              </lift:Menu.item></li>
          <li><lift:Menu.item name="Database" donthide="true" linktoself="true">
                Database
              </lift:Menu.item></li>
        </ul>
      </li>
    </ul>
  }
}

object UserTopbar {
  def render = {
    User.currentUser match {
      case Full(user) =>
        <div class="topContent">
          <fieldset class="">
            <label>Welcome  { user.username.is } { Gravatar.imgTag(user.email.is, 60) }</label>
          </fieldset>
        </div>
        <div id="menu">
          <fieldset class="">
            <ul class="menu">
              <li><label><a href={ "/user/" + (user.username.is.replace(".", "%2E")) }>
                           <span>Profile</span>
                         </a></label></li>
              <li><label><lift:Menu.item name="Account" donthide="true" linktoself="true">
                           <span>Account Setting</span>
                         </lift:Menu.item></label></li>
              <li><label><lift:Menu.item name="Password" donthide="true" linktoself="true">
                           <span>Password Setting</span>
                         </lift:Menu.item></label></li>
              <li><label><lift:Menu.item name="Reminder" donthide="true" linktoself="true">
                           <span>Birthday Reminder</span>
                         </lift:Menu.item></label></li>
              <li><label><lift:Menu.item name="Chat" donthide="true" linktoself="true">
                           <span>Chat</span>
                         </lift:Menu.item></label></li>
              <li><label><lift:Menu.item name="About" donthide="true" linktoself="true">
                           <span>Help</span>
                         </lift:Menu.item></label></li>
              <li><label><lift:Menu.item name="Logout" donthide="true" linktoself="true">
                           <span>Log Out</span>
                         </lift:Menu.item></label></li>
            </ul>
          </fieldset>
        </div>
      case _ if (S.request.flatMap(_.location).map(_.name).filterNot(
        it => List("Login", "Register").contains(it)).isDefined) =>
        <div class="box login">
          <fieldset class="boxBody">
            <label>
              This is LiftWeb Login Template .  If you want to use Liftweb as framework , 
      		  Scala as Programming Language and MongoDB as Database then 
      		  This demo project can be used as a starting point for your application .
              <p>
                Go To
                <lift:Menu.item name="Login" donthide="true" linktoself="true">
                  <strong>Demo</strong>
                </lift:Menu.item>
                and Have Fun !!!
              </p>
            </label>
          </fieldset>
        </div>
      case _ => NodeSeq.Empty
    }
  }
}


