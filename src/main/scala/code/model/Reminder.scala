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
  object friend_name extends StringField(this, 1200)
  object created extends DateField(this)
  object dob extends DateField(this)

}

object Reminder extends Reminder with MongoMetaRecord[Reminder] {

  /**
   * For creating Friend's Birthday Reminder.
   */
  def createReminder(id: String, friend_name: String, dob: String): Either[String, Boolean] = {
    if (friend_name.equals(""))
      Left("Enter Friend's Name")
    else if (dob.equals(""))
      Left("Select Date")
    else {
      try {
        val df = new SimpleDateFormat("MM/dd/yyyy");
        df.setLenient(false);
        val position = new ParsePosition(0);
        val stringToDate = df.parse(dob, position);
        if (position.getIndex() == dob.length()) {
          val reminder = Reminder.createRecord
          reminder.created(new Date())
          reminder.dob(stringToDate)
          reminder.owner(new ObjectId(id))
          reminder.friend_name(friend_name)
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
        case _ => Reminder.findAll((("owner" -> userId)), ("dob" -> sortOrder), Skip(skip), Limit(limit))
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
  def updateReminder(reminder: Reminder, friend_name: String, date: String): Either[String, Boolean] = {
    if (friend_name.equals(""))
      Left("Can not updated. Enter Friend's Name")
    else if (date.equals(""))
      Left("Can not updated. Select Date")
    else {
      try {
        val df = new SimpleDateFormat("MM/dd/yyyy");
        df.setLenient(false);
        val position = new ParsePosition(0);
        val stringToDate = df.parse(date, position);
        if (position.getIndex() == date.length()) {
          reminder.friend_name(friend_name).dob(stringToDate).update
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
    Reminder.findAll(MongoDBObject("dob" -> MongoDBObject("$gte" -> start_date, "$lte" -> end_date), "owner" -> new ObjectId(userID)))
  }
}

