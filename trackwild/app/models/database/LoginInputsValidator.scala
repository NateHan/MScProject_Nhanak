package models.database

import java.sql.{Connection, Statement}

import models.formdata.UserLoginData
import play.api.db.Database

import scala.collection.mutable.ListBuffer

/**
  * Class to determine if login field inputs correlate to a user in the DB.
  * @param twDB the current default database for the application
  * @param userData created from user's POST request
  * Created by nathanhanak on 7/12/17.
  */
class LoginInputsValidator (twDB : Database, userData: UserLoginData) extends DbInputValidator {

  // String representation of inputs which were not valid in the database
  var invalidInputs = new ListBuffer[String]()

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

  /**
    * Determines if user's entered email matches with a corresponding record in the DB
    * @param userName the user's entered email
    * @param stmt a statement object derived from the database
    * @return true if there is a match, false if not
    */
  def userNameIsValid(userName: String, stmt: Statement): Boolean = {
    val qryResult = stmt.executeQuery(s"select email from verified_users where email='$userName';")
    var result : Boolean = false
    while(qryResult.next()) {
      if (qryResult.getString("email").contains(userName)) result = true
      }
    if (!result) {invalidInputs += userName}
    return result
  }

  /**
    * Determine's if a user's password corresponds to a database entry for their password
    * @param pass the user's entered password string
    * @param stmt a statement object derived from the database
    * @return true if there is a match, false if not
    */
  def userPassIsValid(pass: String, stmt: Statement): Boolean = {
    val qryResult = stmt.executeQuery(s"select password from verified_users where password='$pass';")
    var result : Boolean = false
    while(qryResult.next()) {
      if (qryResult.getString("password").contains(pass)) result = true
    }
    if (!result) {invalidInputs += pass}
    return result
  }

  /**
    * retrieves list of invalid inputs entered into the login form
    * @return the List of invalid input fields in String representation
    */
  override def getInvalidInputs(): List[String] = invalidInputs.toList
}
