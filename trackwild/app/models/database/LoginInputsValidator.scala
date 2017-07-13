package models.database

import java.sql.{Connection, Statement}

import models.formdata.UserLoginData
import play.api.db.Database

/**
  * Class to determine if login field inputs correlate to a user in the DB.
  * @param twDB the current default database for the application
  * @param userData created from user's POST request
  * Created by nathanhanak on 7/12/17.
  */
class LoginInputsValidator (twDB : Database, userData: UserLoginData) extends DbInputValidator {

  // String representation of inputs which were not valid in the database
  var invalidInputs : List[String] = _

  /**
    * Verifies login information correctly matches a database entry
    * @return true if valid, false if invalid inputs > 0
    */
  override def inputsAreValid(): Boolean = {
    twDB.withConnection { conn =>
      val stmt = conn.createStatement()
      if (userNameIsValid(userData.inputEmail, stmt) && userPassIsValid(userData.inputPassword, stmt)) true else false
    }
  }
  
  def userNameIsValid(userName: String, stmt: Statement): Boolean = {
    val qryResult = stmt.executeQuery(s"select email from verified_users where email='$userName';")
    var result : Boolean = false
    while(qryResult.next()) {
      if (qryResult.getString("email").contains(userName)) result = true else {
        invalidInputs + userName
        result = false
      }
    }
    return result
  }

  def userPassIsValid(pass: String, stmt: Statement): Boolean = {
    val qryResult = stmt.executeQuery(s"select password from verified_users where password='$pass';")
    var result : Boolean = false
    while(qryResult.next()) {
      if (qryResult.getString("password").contains(pass)) result = true else {
        invalidInputs + pass
        result = false
      }
    }
    return result
  }

  /**
    * retrieves list of invalid inputs entered into the login form
    * @return the List of invalid input fields in String representation
    */
  override def getInvalidInputs(): List[String] = invalidInputs
}
