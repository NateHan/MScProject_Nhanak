package models.forms

import models.database.{DbInputValidator, LoginInputsValidator}

/**
  * Created by nathanhanak on 7/10/17.
  */
case class UserLoginData(email:String, pass:String, rememberLogin:Boolean) {

}



