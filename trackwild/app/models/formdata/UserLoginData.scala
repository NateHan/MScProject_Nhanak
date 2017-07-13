package models.formdata

import models.database.{DbInputValidator, LoginInputsValidator}

/**
  * Created by nathanhanak on 7/10/17.
  */
case class UserLoginData(inputEmail:String, inputPassword:String, rememberLogin:Boolean) {

}



