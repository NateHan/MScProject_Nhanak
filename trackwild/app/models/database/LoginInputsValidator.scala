package models.database
import javax.inject.Inject

import play.api.data.Form
import play.api.db.Database

/**
  * Class to determine if login field inputs correlate to a user in the DB.
  *
  * Created by nathanhanak on 7/12/17.
  */
class LoginInputsValidator[UserInputForm] (twDB : Database) extends DbInputValidator[UserInputForm] {

  // String representation of inputs which were not valid in the database
  val invalidInputs : List[String] = null

  /**
    * Verifies login information correctly matches a database entry
    * @param form created from user's POST request
    * @return true if valid, false if invalid inputs > 0
    */
  override def inputsAreValid(form: Form[UserInputForm]): Boolean = {

  }

  /**
    * retrieves list of invalid inputs entered into the login form
    * @return the List of invalid input fields in String representation
    */
  override def getInvalidInputs(): List[String] = invalidInputs
}
