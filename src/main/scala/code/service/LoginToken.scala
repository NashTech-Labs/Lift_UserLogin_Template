package code.service

import net.liftweb.json.JsonDSL._
import net.liftweb.common.Loggable
import code.model.User
import java.net.URLDecoder
import net.liftweb.common.Empty
import net.liftweb.common.Box
import net.liftweb.common.Full
import java.sql.Timestamp
import java.util.Date

/**
 * This class is for generating and fetching
 * user token
 */
object LoginToken extends Loggable {

  def getID(token: String): String =
    {
      val v = User.find(("token" -> token) ~ Nil)
      if (v.isEmpty) ""
      else
        v.get.id.toString()
    }

  def getUserFromOauthToken(token: String): Box[User] =
    {
      User.find(("token" -> token))
    }

  /**
   *  Get  Token In Exchange of Email and Password.
   */
  def getToken(mail: String, pass: String): Box[String] = {
    val email = URLDecoder.decode(mail, "UTF-8");
    val user = User.findByEmail(email)
    if (!user.isEmpty) {
      val usr = user.get
      if (usr.password.isMatch(pass)) {
        processToken(usr)
      } else Empty
    } else Empty
  }

  def processToken(usr: User): Box[String] =
    {
      if (usr.token.is.length > 10) {
        logger.info("user already have the Token")
        Full(usr.token.value)
      } else {
        val token = generateToken(usr.email.is)
        User.update(("email" -> usr.email.is) ~ Nil, ("$set" -> ("token" -> token) ~ Nil))
        Full(token)
      }
    }

  def generateToken(user: String): String =
    {
      logger.info("create a Token for " + user)
      val date = new Date()
      val timeStamp = new Timestamp(date.getTime)
      val token = bytesToHex((user.hashCode + timeStamp.hashCode() + 41).toString.getBytes.toList)
      logger.info("Token " + token)
      token
    }

  def bytesToHex(bytes: List[Byte]) =
    bytes.map { b ⇒ String.format("%02X", java.lang.Byte.valueOf(b)) }.mkString("")

}