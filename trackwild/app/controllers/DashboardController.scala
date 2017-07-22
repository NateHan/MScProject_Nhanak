package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/14/17.
  */
@Singleton
class DashboardController @Inject()(authController: AuthenticationController, cc: ControllerComponents) extends AbstractController(cc) {


  def index() = Action {
    implicit request: Request[AnyContent] => authController.returnDesiredPageIfAuthenticated(
      request, views.html.afterLogin.dashboard(), "Login failed")
  }
}
