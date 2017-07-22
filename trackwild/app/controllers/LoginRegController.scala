package controllers

import javax.inject.{Inject, Singleton}

import models.database.{DbInputValidator, LoginInputsValidator}
import models.formdata.UserLoginData
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/7/17.
  */
@Singleton
class LoginRegController @Inject()(twDB: Database, cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {


  def loadLogin() = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.login(loginform)).withHeaders(SecurityHeadersFilter
        .CONTENT_SECURITY_POLICY_HEADER -> " .fontawesome.com .fonts.googleapis.com")
  }

  val loginform: Form[UserLoginData] = Form(
    mapping(
      "inputEmail" -> nonEmptyText,
      "inputPassword" -> nonEmptyText,
      "rememberLogin" -> boolean
    )(UserLoginData.apply)(UserLoginData.unapply)
  )

  /**
    * runs when user clicks login, sending a post request with the data from the login fields
    * Runs user through authentication check, if passes attaches user details to
    * session cookie
    * @return the Action for the resulting page
    */
  def attemptLogin() = Action {
    implicit request: Request[AnyContent] => {
      loginform.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.login(formWithErrors)),
        successfulForm => {
          val validator: DbInputValidator = new LoginInputsValidator(twDB, loginform.bindFromRequest().get)
          if (validator.inputsAreValid) {
            val userEmail = loginform.bindFromRequest().get.inputEmail
            Ok(views.html.afterLogin.dashboard())
              .withSession("authenticated" -> "true", "username" -> getUserName(userEmail))
          } else {
            BadRequest(views.html.login(loginform))
          }
        }
      )
    }
  }

  /**
    * Brings user to landing page, giving them a new session cookie to fully logout.
    * @return
    */
  def logOut = Action { Ok(views.html.index()).withNewSession}

  /**
    * retrieves the user name for display in the logged in navbar
    *
    * @param email the successfully logged in email handle for the user
    * @return the username in string form
    */
  private def getUserName(email: String): String = {
    twDB.withConnection { conn =>
      val qryResult = conn.createStatement.executeQuery(s"SELECT userName FROM verified_users WHERE uemail='$email';")
      var userName: String = ""
      while (qryResult.next()) {
        userName = qryResult.getString("userName")
      }
      userName
    }
  }

}

