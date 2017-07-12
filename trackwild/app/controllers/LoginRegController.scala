package controllers

import javax.inject.{Inject, Singleton}

import models.forms.UserLoginData
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/7/17.
  */
@Singleton
class LoginRegController @Inject()(twDB : Database, cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {


  def loadLogin() = Action {
    implicit request: Request[AnyContent] => Ok(views.html.login(loginform)).withHeaders(SecurityHeadersFilter
      .CONTENT_SECURITY_POLICY_HEADER -> " .fontawesome.com .fonts.googleapis.com")
  }

  val loginform: Form[UserLoginData] = Form(
    mapping(
      "inputEmail" -> nonEmptyText,
      "inputPassword" -> nonEmptyText,
      "rememberLogin" -> boolean
    )(UserLoginData.apply)(UserLoginData.unapply)
  )

  def attemptLogin() = Action {
    implicit request: Request[AnyContent] =>
      loginform.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.login(formWithErrors)),
        customer => if (credentialsNotFound(loginform).isEmpty) { Ok(views.html.success()) } else {
          credentialsNotFound(loginform).foreach( str => println(str)) // remove after debugging later
          Ok(views.html.login(loginform))
        }
      )
  }

  /**
    * Returns any login credentials which were not contained in the DB
    * @param form the form containing the input credentials
    * @return a List[String] of the inputs which did not contain entries in the database, empty
    *         if all were valid
    */
  def credentialsNotFound(form: Form[UserLoginData]): List[String] = {
    val userData = loginform.get
    val userEmail = userData.email
    val userPass = userData.pass
  }
}

