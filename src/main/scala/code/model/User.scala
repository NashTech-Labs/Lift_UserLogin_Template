package code {
  package model {

    import java.util.TimeZone
    import org.bson.types.ObjectId
    import org.joda.time.DateTime
    import code.config.Site
    import net.liftmodules.mongoauth.field._
    import net.liftmodules.mongoauth.model._
    import net.liftmodules.mongoauth._
    import net.liftweb.common._
    import net.liftweb.http.LiftResponse
    import net.liftweb.http.RedirectResponse
    import net.liftweb.http.Req
    import net.liftweb.http.S
    import net.liftweb.http.SessionVar
    import net.liftweb.json.JsonAST.JField
    import net.liftweb.json.JsonAST.JInt
    import net.liftweb.json.JsonAST.JObject
    import net.liftweb.json.JsonAST.JString
    import net.liftweb.json.JsonAST.JValue
    import net.liftweb.mongodb.record.field._
    import net.liftweb.record.field._
    import net.liftweb.util.FieldContainer
    import net.liftweb.util.Helpers
    import net.liftweb._
    import net.liftweb.http.SessionVar
    import net.liftweb.util.Props
    import code.lib.FacebookGraph

    /**
     * 	A user who uses our application.
     */
    class User private () extends ProtoAuthUser[User] with ObjectIdPk[User] {
      def meta = User

      def cities = location.is.split(',').map(_.trim).filter(""==)

      /**
       * String representing the User ID
       */
      def userIdAsString: String = id.toString

      /**
       * User Locale which can be used for localization
       * override locale with default value "en"
       */

      object locale extends LocaleField(this) {
        override def displayName = "Locale"
        override def defaultValue = "en"
      }

      /**
       *  Override User Time zone with
       *  default value 'America/Chicago'
       */
      object timezone extends TimeZoneField(this) {
        override def displayName = "Time Zone"
        override def defaultValue = "America/Chicago"
      }

      /**
       * Token field can be used for
       * such functionality like Forget Password
       */
      object token extends StringField(this, 64) {
        override def displayName = "Access_Token"
        override def defaultValue = ""
      }

      /**
       * Override name field's display name
       * and validation property
       */
      object name extends StringField(this, 64) {
        override def displayName = "Name"

        override def validations =
          valMaxLen(64, "Name must be 64 characters or less") _ ::
            super.validations
      }
      /**
       * Override location field's display name
       * and validation property
       */
      object location extends StringField(this, 64) {
        override def displayName = "Location"

        override def validations =
          valMaxLen(64, "Location must be 64 characters or less") _ ::
            super.validations
      }
      /**
       * Override bio field's display name
       * and validation property
       */
      object bio extends TextareaField(this, 160) {
        override def displayName = "Bio"

        override def validations =
          valMaxLen(160, "Bio must be 160 characters or less") _ ::
            super.validations
      }
      object facebookId extends IntField(this) {
        override def optional_? = true
      }

      /**
       * FieldContainers for various LiftScreeens.
       */
      def accountScreenFields = new FieldContainer {
        def allFields = List(username, email, locale, timezone)
      }

      def profileScreenFields = new FieldContainer {
        def allFields = List(name, location, bio)
      }

      def registerScreenFields = new FieldContainer {
        def allFields = List(username, email)
      }

      def whenCreated: DateTime = new DateTime(id.is.getTime)
      def isConnectedToFacebook: Boolean = facebookId.is > 0
    }

    /**
     * U S E R   O B J E C T
     */

    object User extends User with ProtoAuthUserMeta[User] with Loggable {
      import mongodb.BsonDSL._

      override def collectionName = "user.users"

      // Ensures that and index is created on email and username and entries are unique
      ensureIndex((email.name -> 1), true)
      ensureIndex((username.name -> 1), true)

      def findByEmail(in: String): Box[User] = find(email.name, in)

      def findByUsername(in: String): Box[User] = find(username.name, in)

      def findByFacebookId(in: Int): Box[User] = find(facebookId.name, in)

      def findCurrentUser = User.currentUser match {
        case Full(user) => user
        case Empty => User
        case Failure(msg, _, _) =>
          logger.warn("Error logging user : %s".format(msg))
          User
      }

      def findByStringId(id: String): Box[User] =
        if (ObjectId.isValid(id)) find(new ObjectId(id)) else Empty

      override def onLogIn: List[User ⇒ Unit] =
        List(user ⇒ User.loginCredentials.remove())

      override def onLogOut: List[Box[User] ⇒ Unit] = List(
        x ⇒ logger.debug("User.onLogOut called."),
        boxedUser ⇒ boxedUser.foreach { u ⇒
          ExtSession.deleteExtCookie()
        })

      /**
       * MongoAuth variables
       */
      private lazy val siteName = MongoAuth.siteName.vend
      private lazy val sysUsername = MongoAuth.systemUsername.vend
      private lazy val indexUrl = MongoAuth.indexUrl.vend
      private lazy val loginTokenAfterUrl = MongoAuth.loginTokenAfterUrl.vend

      /**
       * User CRUD functionality
       */

      /**
       * Create a new user
       */
      def create(email: String) = {
        val user = User.createRecord
        user.email(email)
        user
      }

      /**
       * Update password for a user
       */
      def updatePassword(user: User, old_pass: String, new_pass: String) = {
        if (user.password.isMatch(old_pass)) {
          user.password(new_pass)
          user.password.hashIt
          user.update
          true
        } else {
          false
        }
      }

      /**
       * Token related functionality
       */
      private def logUserInFromToken(uid: ObjectId): Box[Unit] = find(uid).map { user ⇒
        user.verified(true)
        user.save
        logUserIn(user, false)
        LoginToken.deleteAllByUserId(user.id.is)
      }

      override def handleLoginToken: Box[LiftResponse] = {
        var respUrl = indexUrl.toString
        S.param("token").flatMap(LoginToken.findByStringId) match {
          case Full(at) if (at.expires.isExpired) => {
            S.error("Login token has expired")
            at.delete_!
          }
          case Full(at) => logUserInFromToken(at.userId.is) match {
            case Full(_) => respUrl = loginTokenAfterUrl.toString
            case _ => S.error("User not found")
          }
          case _ => S.warning("Login token not provided")
        }

        Full(RedirectResponse(respUrl))
      }

      /**
       * send an email to the user
       *  with a link for logging in
       */
      def sendLoginToken(user: User): Unit = {
        import net.liftweb.util.Mailer._

        val token = LoginToken.createForUserId(user.id.is)

        val msgTxt =
          """
        	|Someone requested a link to change your password on the %s website.
        	|
        	|If you did not request this, you can safely ignore it. 
          	|It will expire 48 hours from the time this message was sent.
        	|
        	|Follow the link below or copy and paste it into your internet browser.
        	|
        	|%s
        	|
        	|Thanks,
        	|%s
        """.format(siteName, token.url, sysUsername).stripMargin

        /*sendMail(
          From(MongoAuth.systemFancyEmail),
          Subject("%s Password Help".format(siteName)),
          To(user.fancyEmail),
          PlainMailBodyType(msgTxt))*/
      }

      def loginUser(user: User) = {
        logUserIn(user, true, true)
      }

      /**
       * ExtSession
       */
      private def logUserInFromExtSession(uid: ObjectId): Box[Unit] =
        find(uid).map { user =>
          logUserIn(user, false)
        }

      def createExtSession(uid: ObjectId) = ExtSession.createExtSession(uid)

      /**
       * Test for active ExtSession.
       */
      def testForExtSession: Box[Req] => Unit = {
        ignoredReq =>
          {
            if (currentUserId.isEmpty) {
              ExtSession.handleExtSession match {
                case Full(es) => logUserInFromExtSession(es.userId.is)
                case Failure(msg, _, _) =>
                  logger.warn("Error logging user in with ExtSession: %s".format(msg))
                case Empty =>
              }
            }
          }
      }

      /**
       * Verify User and create Login session
       * @param email of the user
       * @param password entered by user
       * @param haspassword : if no , an email would be send to user with new password
       * @remember : To remember user
       * @returns Either Boolean or String
       */

      def process(
        e: String,
        password: String,
        hasPassword: Boolean,
        remember: Boolean): Either[String, Boolean] = {

        val email = e.toLowerCase.trim
        // save the email and remember entered in the session var
        User.loginCredentials(LoginCredentials(email, remember))

        if (hasPassword && email.length > 0 && password.length > 0) {
          User.findByEmail(email) match {
            case Full(user) if (user.password.isMatch(password)) =>
              User.loginUser(user)
              if (remember) User.createExtSession(user.id.is)
              Right(true)
            case _ => Left("Invalid credentials.")
          }
        } else if (hasPassword && email.length <= 0 && password.length > 0)
          Left("Please enter an email.")
        else if (hasPassword && password.length <= 0 && email.length > 0)
          Left("Please enter a password.")
        else if (hasPassword)
          Left("Please enter an email and password.")
        else
          Left("Please enter an email address")
      }

      /**
       * If user does not have password
       */

      def checkPassowrd(email: String, hasPassword: Boolean): Either[String, Boolean] = {
        hasPassword match {
          case false => {
            User.findByEmail(email) match {
              case Full(user) => {
                User.loginCredentials.remove()
                Left("An email has been sent to you with instructions for accessing your account.")
              }
              case _ => Right(false)
            }
          }
          case _ => Right(true)
        }
      }
      /**
       * used during login process
       */
      object loginCredentials extends SessionVar[LoginCredentials](LoginCredentials(""))
      object regUser extends SessionVar[User](createRecord.email(loginCredentials.is.email))

      /**
       * where to send the user after logging in/registering
       */
      object loginContinueUrl extends SessionVar[String](Site.home.url)
      def createUserFromCredentials = createRecord.email(loginCredentials.is.email)
      def isConnectedToFaceBook: Boolean = currentUser.map(_.isConnectedToFacebook).openOr(false)

      def fromFacebookJson(in: JValue): Box[User] = {
        Helpers.tryo(
          in transform {
            case JField("id", JString(id)) ⇒ JField("facebookId", JInt(Helpers.toInt(id)))
            case JField("first_name", JString(fname)) ⇒ JField("firstName", JString(fname))
            case JField("last_name", JString(fname)) ⇒ JField("lastName", JString(fname))
            case JField("location", JObject(List(JField("facebookId", JInt(id)), JField("name", JString(name))))) ⇒
              JField("location", JString(name))
            case JField("timezone", JInt(offset)) ⇒
              val offId = "GMT" + (
                if (offset > 0)
                  "+%s".format(offset.toString)
                else
                  "%s".format(offset.toString))
              JField("timezone", JString(TimeZone.getTimeZone(offId).getID))
          }) flatMap { jv ⇒
            //logger.debug(pretty(render(jv)))
            fromJValue(jv)
          }
      }
      def disconnectFacebook(in: User): Unit = {
        // remove the facebookId
        val qry = (id.name -> in.id.is)
        val upd = ("$unset" -> (facebookId.name -> 1))
        User.update(qry, upd)
      }

      def disconnectFacebook(): Box[Unit] = {
        User.currentUser.flatMap { user ⇒
          // call facebook api to deauthorize
          FacebookGraph.deletePermission(user.facebookId.is)
            .filter(_ == true).map { x ⇒
              disconnectFacebook(user)
              FacebookGraph.currentAccessToken.remove()
            }
        }
      }

      def saveUser(email: String, password: String, name: String, userName: String): User = {
        val user = create(email)
        user.name(name)
        user.username(userName)
        user.verified(false)
        user.password(password)
        user.password.hashIt
        user.save
      }

      def createUser(email: String, password: String): Either[String, Boolean] = {
        User.findByEmail(email) match {
          case Full(user) => Right(false)
          case _ => {
            saveUser(email, password, "", email)
            Right(true)
          }
        }
      }
    }
    
  

    case class LoginCredentials(val email: String, val isRememberMe: Boolean = false)
  }
}

  