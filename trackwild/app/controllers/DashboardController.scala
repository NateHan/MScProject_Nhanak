package controllers

import java.sql.SQLException
import javax.inject.{Inject, Singleton}

import models.database.{DatabaseUpdate, ProjectsUserCollabs, ProjectsUserLeads}
import models.formdata.NewProjectData
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.twirl.api.HtmlFormat

/**
  * Created by nathanhanak on 7/14/17.
  */
@Singleton
class DashboardController @Inject()(twDB: Database, authController: AuthenticationController, cc: ControllerComponents) extends AbstractController(cc)
  with play.api.i18n.I18nSupport {

  /**
    * The first page to load after a user logs in
    *
    * @return a Result containing the dashboard home page
    */
  def index() = Action {
    implicit request: Request[AnyContent] =>
      authController.returnDesiredPageIfAuthenticated(
        request, views.html.afterLogin.dashboardviews.dashboard(), "Login failed")
  }

  /**
    * Loads the page which contains the options for users to interact with their projects:
    * The projects they lead
    * The projects they are invited to collaborate on
    * Or create a new project
    *
    * @return
    */
  def loadUserProjects() = Action {
    implicit request: Request[AnyContent] =>
      authController.returnDesiredPageIfAuthenticated(
        request, views.html.afterLogin.dashboardviews.userProjects(), "Credentials Expired")
  }

  /**
    * Method is response to when user clicks a slider, returns a page to be loaded
    * into the slider's content div
    * @param page one of the possible options to load in the userProjects.scala.html sliders
    * @return the  dashboardviews.___ templates to return
    */
  def projectOptionPicker(page: String) = Action {
    implicit request: Request[AnyContent] =>
      val pageToLoad = page match {
        case "userLead" => loadUserLeadProjectsView(request.session.get("username").getOrElse("not logged in"))
        case "userCollab" => loadUserCollabProjectsView(request.session.get("username").getOrElse("not logged in"))
        case "newProject" => views.html.afterLogin.dashboardviews.newProjectCreator(newProjForm)
      }
      authController.returnDesiredPageIfAuthenticated(request, pageToLoad, "Credentials Expired")
  }

  val newProjForm: Form[NewProjectData] = Form {
    mapping(
      "title" -> nonEmptyText,
      "userName" -> nonEmptyText,
      "initialNote" -> nonEmptyText
    )(NewProjectData.apply)(NewProjectData.unapply)
  }

  /**
    * Method responsible for the creation of a new project from the dashboard.
    * From the user form, it creates a new project within all_projects DB table and then
    * inserts a new note in the project_notes.
    * @return an Ok response if successful, a BadRequest if there is a failure.
    */
  def postNewProject() = Action {
    implicit request: Request[AnyContent] =>
      newProjForm.bindFromRequest().fold(
        errorForm => BadRequest(s"Project Creation Failed: Form Data Did Not Bind"),
        successForm => {
          val colsToValsProjects: Map[String, String] = Map(
            "project_title" -> successForm.title,
            "project_lead" -> successForm.userName)
          val colsToValsNotes: Map[String, String] = Map(
            "project_title" -> successForm.title,
            "note_title" -> s"${successForm.userName} created project.",
            "note_author" -> successForm.userName,
            "note_content" -> successForm.initialNote
          )
          try {
            val rowsInsertedProjects = DatabaseUpdate.insertRowInto(twDB, "all_projects", colsToValsProjects)
            val rowsInsertedNotes = DatabaseUpdate.insertRowInto(twDB, "project_notes", colsToValsNotes)
            if (rowsInsertedProjects == 1 && rowsInsertedNotes == 1) {
              Ok(Json.obj("newProjectName" -> successForm.title))
            }
            else BadRequest(s"Project Creation Failed: Did not get expected SQL Response")
          } catch {
            case e: SQLException => e.printStackTrace; BadRequest(s"Project Creation Failed - SQL Error: ${e.getCause.toString}")
            case unknown : Throwable => unknown.printStackTrace; BadRequest(s"Project Creation Failed - Unknown Error: ${unknown.getCause.toString}")
          }
        }
      )
  }

  /**
    * Returns the template needed for when a User attempts to create a new project. Template
    * is dynamic and constructor of template determines which type of message to display to user
    * based on the response
    * @param response the response for whether or not the project failed or succeeded
    * @param projectTitle the name of the project which the user attempted to create
    * @return the sliderResponse template, loaded with the resulting constructor
    */
  def getSliderResponse(response:String, projectTitle:String) = Action {
    implicit request: Request[AnyContent] =>
      response match {
        case "newProjSuccess" => Ok(views.html.afterLogin.dashboardviews.sliderResponse(response, projectTitle))
        case "newProjFail" => Ok(views.html.afterLogin.dashboardviews.sliderResponse(response, projectTitle))
        case _ => Ok(views.html.afterLogin.dashboardviews.sliderResponse(response, projectTitle))
      }
  }

  /**
    * Retrieves the view which contains all user lead projects
    * @param userName the current user from the session
    * @return an HTML view template which will be a table of all the projects the user leads
    */
  def loadUserLeadProjectsView(userName : String) = {
      val allProjects: List[Array[String]] = ProjectsUserLeads.getAll(userName, twDB)
      views.html.afterLogin.dashboardviews.userLeadProjects(allProjects)
  }

  /**
    * Retrieves the view which contains all projects in which user collaborates but does not lead
    * @param userName the name of the current user
    * @return an HTML view template which will contain a table of all user collaborated projects
    */
  def loadUserCollabProjectsView(userName: String) = {
   val allCollabProjects: List[Array[String]] = ProjectsUserCollabs.getAllCollabs(userName, twDB)
   views.html.afterLogin.dashboardviews.userCollabProjects(allCollabProjects)
  }


}
