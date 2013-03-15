package code.snippet

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import code.model.User
import com.mongodb.casbah.commons.MongoDBObject
import code.config.MongoConfig
import code.model.Reminder
import java.text.SimpleDateFormat
import code.model.LoginCredentials
import net.liftweb.http.LiftSession
import net.liftweb.util.StringHelpers
import net.liftweb.common.Empty
import net.liftweb.http.S

@RunWith(classOf[JUnitRunner])
class ThingsToDoTest extends FunSuite with BeforeAndAfter {

  before {
    MongoConfig.init()
  }

  /**
   * Test case for creation of things to do
   */

  test("Testing for a successful creation of Things to do") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    val user = User.saveUser("test@test.com", "123456", "test", "test")
    val validate = Reminder.createThingsToDo(user.id.toString, "Have to play cricket in morning", "03/12/2013")
    assert(validate.isRight == true)
  }

   test("Testing for a unsuccessful creation of Things to do with blank text") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      val validate = Reminder.createThingsToDo(user.id.toString,"", "03/12/2013")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful creation of Things to do with a blank date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      val validate = Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful creation of Things to do with a blank text and blank date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      val validate = Reminder.createThingsToDo(user.id.toString,"", "")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful creation of Things to do with a blank text and incorrect format of date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      val validate = Reminder.createThingsToDo(user.id.toString,"", "12 mar 2013")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful creation of Things to do with incorrect format of date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      val validate = Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "12 mar 2013")
      assert(validate.isLeft == true)
    }
  }

  
  /**
   * Test case for deletion of things to do
   */ 

  test("Testing for a successful deletion of Things to do") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.deleteThingsToDo(thingsToDo.open_!)
      assert(validate)
    }
  }

  
  /**
   * Test case for updating things to do
   */ 

  test("Testing for a successful updation of Things to do") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.updateThingsToDo(thingsToDo.open_!, "Have to play cricket in morning", "03/12/2013")
      assert(validate.isRight == true)
    }
  }

  test("Testing for a unsuccessful updation of Things to do with blank text") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.updateThingsToDo(thingsToDo.open_!, "", "03/12/2013")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful updation of Things to do with a blank date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.updateThingsToDo(thingsToDo.open_!, "Have to play cricket in morning", "")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful updation of Things to do with a blank text and blank date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.updateThingsToDo(thingsToDo.open_!, "", "")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful updation of Things to do with a blank text and incorrect format of date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.updateThingsToDo(thingsToDo.open_!, "", "12 mar 2013")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful updation of Things to do with incorrect format of date") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.updateThingsToDo(thingsToDo.open_!, "Have to play cricket in morning", "12 mar 2013")
      assert(validate.isLeft == true)
    }
  }

  test("Testing for a unsuccessful updation of Things to do, if status is true") {
    val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
    S.initIfUninitted(session) {
      val user = User.saveUser("test@test.com", "123456", "test", "test")
      User.logUserIn(user, true)
      Reminder.createThingsToDo(user.id.toString,"Have to play cricket in morning", "03/12/2013")
      val thingsToDo = Reminder.find(MongoDBObject("description" -> "Have to play cricket in morning"))
      val validate = Reminder.updateThingsToDo(thingsToDo.open_!, "Have to play cricket in morning", "03/12/2013")
      assert(validate.isLeft == true)
    }
  }
  
  after {
    User.delete(MongoDBObject("name" -> "test"))
    Reminder.delete(MongoDBObject("description" -> "Have to play cricket in morning"))
    Reminder.delete(MongoDBObject("description" -> ""))

  }
}