package code.snippet
/**
 * @author ayush
 */
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import code.model.User
import com.mongodb.casbah.commons.MongoDBObject
import code.config.MongoConfig

@RunWith(classOf[JUnitRunner])
class UserLoginTest extends FunSuite with BeforeAndAfter {

  before {
    MongoConfig.init()
    val user = User.saveUser("test@test.com", "123456", "test", "test")
  }

  test("Testing for a successful login") {
    val validateUser = User.process("test@test.com", "123456", true, true)
    assert(validateUser.isRight == true)
  }

  test("Testing for a unsuccessful login with a blank password") {
    val validateUser = User.process("test@test.com", "", true, true)
    assert(validateUser.isLeft == true)
  }

  test("Testing for a unsuccessful login with a blank email") {
    val validateUser = User.process("", "1234", true, true)
    assert(validateUser.isLeft == true)
  }

  test("Testing for a unsuccessful login with a blank email and blank password") {
    val validateUser = User.process("", "", true, true)
    assert(validateUser.isLeft == true)
  }

  test("Testing for a unsuccessful login with a wrong email and wrong password") {
    val validateUser = User.process("a@a.com", "1234", true, true)
    assert(validateUser.isLeft == true)
  }

  after {
    User.delete(MongoDBObject("name" -> "test"))
  }
}