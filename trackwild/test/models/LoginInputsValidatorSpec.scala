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
    driver = "org.postgresql.Driver",
    url = "postgres://twadmin:trackwild@localhost:5432/track_wild_db"
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
  val registeredUserData = UserLoginData("nathan.hanak@gmail.com", "trackwild", false)

  "LoginInputsValidator inputsAreValid()" should {

    "return true when given a valid login" in {
      val validator = new LoginInputsValidator(testDb, registeredUserData)
      validator.inputsAreValid() mustBe true
    }

    "return false when given an unregistered email " in {
      val validator = new LoginInputsValidator(testDb, notRegUserGoodPassword)
      validator.inputsAreValid() mustBe false
    }

    "return false when given a registered email with a bad password" in {
      val validator = new LoginInputsValidator(testDb, regUserBadPassword)
      validator.inputsAreValid() mustBe false
    }
  }

  "LoginInputsValidator getInvalidInputs()" should {

    "return a list containing an invalid email when given an invalid email" in {
      val validator = new LoginInputsValidator(testDb, notRegUserGoodPassword)
      validator.inputsAreValid()
      validator.getInvalidInputs() must have size 1
      validator.getInvalidInputs() must contain("bad@bad.com")
    }

    "return a list containing the password when passed a valid login with invalid password" in {
      val validator = new LoginInputsValidator(testDb, regUserBadPassword)
      validator.inputsAreValid() mustBe false
      validator.getInvalidInputs() must have size 1
      validator.getInvalidInputs() must contain("wrongPassword")
    }

    "return an empty list when all inputs are valid " in {
      val validator = new LoginInputsValidator(testDb, registeredUserData)
      validator.inputsAreValid() mustBe true
      val resultList = validator.getInvalidInputs()
      resultList.foreach(str => println("Invalid input: " + str ))
      resultList mustBe empty
    }
  }

}
