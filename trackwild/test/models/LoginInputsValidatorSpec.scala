package models

import models.database.LoginInputsValidator
import models.formdata.UserLoginData
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.Databases

/**
  * Created by nathanhanak on 7/13/17.
  */
class LoginInputsValidatorSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {


  /**
    * a connection to a test version of the database
    */
  val testDb = Databases(
    driver = "org.postgres.jdbc.Driver",
    url = "postgres://twadmin:trackwild@localhost:5432/track_wild_testdb"
  )

  override def afterAll(): Unit = {
    super.afterAll()
    testDb.shutdown()
  }

  /**
    * sample data which will not match to a DB entry
    */
  val regUserBadPassword = UserLoginData("demo@demo.com", "wrongPassword", true)
  val notRegUserGoodPassword = UserLoginData("bad@bad.com", "demo", false)


  /**
    * sample data which should match to a DB entry
    */
  val registerdUserData = UserLoginData("demo@demo.com", "demo", false)

  "LoginInputsValidator inputsAreValid" should {

    "return true when given a valid login" in {
      val validator = new LoginInputsValidator(testDb, registerdUserData)
      validator.inputsAreValid() mustBe true
    }
  }

}
