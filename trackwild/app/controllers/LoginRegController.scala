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
    implicit request: Request[AnyContent] => {
      val newform = loginform.bindFromRequest()
      loginform.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.login(formWithErrors)),
        customer => {
          val validator : DbInputValidator = new LoginInputsValidator(twDB, loginform.bindFromRequest().get)
          if (validator.inputsAreValid) {
            Ok(views.html.success())
          } else {
            Ok(views.html.login(loginform))
          }
        }

      )
    }
  }

}

