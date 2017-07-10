package controllers

import javax.inject.{Inject, Singleton}

import models.forms.UserLoginForm
import play.api.data.Form
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/7/17.
  */
@Singleton
class LoginRegController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def loadLogin() = Action {
    implicit request: Request[AnyContent] => Ok(views.html.loginreg()).withHeaders(SecurityHeadersFilter
      .CONTENT_SECURITY_POLICY_HEADER -> " .fontawesome.com .fonts.googleapis.com")
  }


}

object LoginRegController {

  val createloginform = Form(
    tuple(
      "email" -> text,
      "password" -> text,
      "rememberlogin" -> boolean
    )
  )
}