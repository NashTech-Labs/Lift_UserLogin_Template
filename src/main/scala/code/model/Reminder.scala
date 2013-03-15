package code.model

import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.common.Loggable
import net.liftweb.common.Loggable
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.util.Helpers._
import org.bson.types.ObjectId
import java.text.ParsePosition
import com.mongodb.casbah.commons.MongoDBObject

class Reminder extends MongoRecord[Reminder] with ObjectIdPk[Reminder] with Loggable {

  def meta = Reminder

  object owner extends ObjectIdRefField(this, User)
  object description extends StringField(this, 1200)
  object created extends DateField(this)
  object due extends DateField(this)

}

object Reminder extends Reminder with MongoMetaRecord[Reminder] {

  /**
   * For creating Friend's Birthday Reminder.
   */
  def createReminder(id: String, description: String, due_date: String): Either[String, Boolean] = {
    if (description.equals(""))
      Left("Enter text")
    else if (due_date.equals(""))
      Left("Select date")
    else {
      try {
        val df = new SimpleDateFormat("MM/dd/yyyy");
        df.setLenient(false);
        val position = new ParsePosition(0);
        val stringToDate = df.parse(due_date, position);
        if (position.getIndex() == due_date.length()) {
          val reminder = Reminder.createRecord
          reminder.created(new Date())
          reminder.due(stringToDate)
          reminder.owner(new ObjectId(id))
          reminder.description(description)
          reminder.save
          Right(true)
        } else
          Left("Enter date in MM/dd/yyyy format")
      } catch {
        case ex => {
          Left("Enter date in MM/dd/yyyy format")
        }
      }
    }
  }

  def findAllNotes(userId: String) = Reminder.findAll((("owner" -> userId)))

  def findAllNotes(userId: String, sortOrder: Int, limit: Int, skip: Int) =
    {
      sortOrder match {
        case 0 => Reminder.findAll((("owner" -> userId)), ("created" -> -1), Skip(skip), Limit(limit))
        case _ => Reminder.findAll((("owner" -> userId)), ("due" -> sortOrder), Skip(skip), Limit(limit))
      }
    }

  /**
   * For deleting reminders of a user.
   */
  def deleteReminder(reminder: Reminder) = {
    Reminder.getReminder(reminder).delete_!
  }

  /**
   * For getting reminders reference of a user.
   */
  def getReminder(reminder: Reminder) = {
    Reminder.find(reminder.id.toString).get
  }

  /**
   * For updating status of particular getReminder of a user.
   */
  def updateReminder(reminder: Reminder, desc: String, date: String): Either[String, Boolean] = {
    if (desc.equals(""))
      Left("Can not updated. Enter Text")
    else if (date.equals(""))
      Left("Can not updated. Select date")
    else {
      try {
        val df = new SimpleDateFormat("MM/dd/yyyy");
        df.setLenient(false);
        val position = new ParsePosition(0);
        val stringToDate = df.parse(date, position);
        if (position.getIndex() == date.length()) {
          reminder.description(desc).due(stringToDate).update
          Right(true)
        } else
          Left("Enter date in MM/dd/yyyy format")
      } catch {
        case ex => {
          Left("Enter date in MM/dd/yyyy format")
        }
      }
    }
  }

  def getReminderForToday(userID: String) = {
    val start_date = new Date(new Date().getYear(), new Date().getMonth(), new Date().getDate())
    val end_date = new Date(new Date().getYear(), new Date().getMonth(), new Date().getDate() + 1)
    Reminder.findAll(MongoDBObject("due" -> MongoDBObject("$gte" -> start_date, "$lte" -> end_date), "owner" -> new ObjectId(userID)))
  }
}

