package models.database
/**
  * Trait used to determine input from user is successfully retrieved from the database
  *
  * Created by nathanhanak on 7/12/17.
  */
trait DbInputValidator {

  def inputsAreValid(): Boolean

  def getInvalidInputs(): List[String]

}
