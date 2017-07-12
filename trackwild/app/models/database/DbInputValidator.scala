package models.database

import javax.inject.Inject

import models.forms.UserLoginData
import play.api.data.Form
import play.api.db.Database

/**
  * Trait used to determine input from user is successfully retrieved from the database
  *
  * Created by nathanhanak on 7/12/17.
  */
trait DbInputValidator[T] {

  def inputsAreValid(form: Form[T]): Boolean

  def getInvalidInputs(): List[String]

}
