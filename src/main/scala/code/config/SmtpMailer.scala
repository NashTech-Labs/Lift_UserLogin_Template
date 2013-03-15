package code
package config

import javax.mail.{
  Authenticator,
  PasswordAuthentication
}
import javax.mail.internet.MimeMessage
import net.liftweb._
import common._
import util._
import Mailer._

/*
 * A Mailer config object that 
 * configues mail
 */
object SmtpMailer extends Loggable {
  def init() {

    var isAuth = Props.get(
      "mail.smtp.auth",
      "false").toBoolean

    if (isAuth) {
      (
        Props.get("mail.smtp.user"),
        Props.get("mail.smtp.pass")) match {
          case (
            Full(username),
            Full(password)) =>
            Mailer.authenticator =
              Full(new Authenticator() {
                override def getPasswordAuthentication =
                  new PasswordAuthentication(username, password)
              })
            logger.info("SmtpMailer inited")
          case _ => logger.error(
            "Username/password not supplied for Mailer.")
        }
    }
  }

  def sendEmail(
    to: String,
    subject: String,
    message: String) {
    val from = Props.get("mail.from", "test@test.com")
    sendMail(
      From(from),
      Subject(subject),
      To(to),
      PlainMailBodyType(message))
  }
}
