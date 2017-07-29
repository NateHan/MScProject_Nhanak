package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

/**
  * Created by nathanhanak on 7/14/17.
  */
@Singleton
class DashboardController @Inject()(authController: AuthenticationController, cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * The first page to load after a user logs in
    * @return a Result containing the dashboard home page
    */
  def index() = Action {
    implicit request: Request[AnyContent] => authController.returnDesiredPageIfAuthenticated(
      request, views.html.afterLogin.dashboard(), "Login failed")
  }

  def loadUserProjects() = Action {
    implicit request: Request[AnyContent] => authController.returnDesiredPageIfAuthenticated(
      request, views.html.afterLogin.userProjects(), "Credentials Expired")
  }
}
