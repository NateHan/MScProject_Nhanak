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
      request, views.html.afterLogin.dashboardviews.dashboard(), "Login failed")
  }

  /**
    * Loads the page which contains the options for users to interact with their projects:
    * The projects they lead
    * The projects they are invited to collaborate on
    * Or create a new project
    * @return
    */
  def loadUserProjects() = Action {
    implicit request: Request[AnyContent] => authController.returnDesiredPageIfAuthenticated(
      request, views.html.afterLogin.dashboardviews.userProjects(), "Credentials Expired")
  }

  def projectOptionPicker(page:String) = Action {
    implicit request: Request[AnyContent] =>
      val pageToLoad = page match {
        case "userLead" => views.html.afterLogin.dashboardviews.userLeadProjects()
        case "userCollab" => views.html.afterLogin.dashboardviews.userCollabProjects()
        case "newProject" => views.html.afterLogin.dashboardviews.newProjectCreator()
      }
    authController.returnDesiredPageIfAuthenticated(request, pageToLoad, "Credentials Expired")
  }


}
