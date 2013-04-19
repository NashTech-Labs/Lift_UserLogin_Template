package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import net.liftmodules.textile.TextileParser
import net.liftweb.common.Empty
import code.model._
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.common.Loggable
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.field.ObjectIdRefField
import net.liftweb.mongodb.record.field.DateField
import net.liftweb.record.field.TextareaField

object Message extends Message  with MongoMetaRecord[Message] {
  
  def getAllMessage ={
    Message.findAll
  }
}

class Message  extends MongoRecord[Message] with ObjectIdPk[Message] with Loggable {
  def meta = Message 
  
  object user extends ObjectIdRefField(this, User)

  object createdAt extends DateField(this)

  object content extends TextareaField(this, 2048) {
    override def textareaRows  = 10
    override def textareaCols = 50
    override def displayName = "Message"
  }

  def contentAsHtml = TextileParser.paraFixer(TextileParser.toHtml(content.is, Empty))
}