package controllers

import java.sql.SQLException
import javax.inject.{Inject, Singleton}

import models.database.DatabaseUpdate
import models.formdata.NewProjectData
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

/**
  * Created by nathanhanak on 7/14/17.
  */
@Singleton
class DashboardController @Inject()(twDB: Database, authController: AuthenticationController, cc: ControllerComponents) extends AbstractController(cc) {

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

  def projectOptionPicker(page: String) = Action {
    implicit request: Request[AnyContent] =>
      val pageToLoad = page match {
        case "userLead" => views.html.afterLogin.dashboardviews.userLeadProjects()
        case "userCollab" => views.html.afterLogin.dashboardviews.userCollabProjects()
        case "newProject" => views.html.afterLogin.dashboardviews.newProjectCreator()
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
        errorForm => BadRequest("Project Creation Failed"),
        successForm => {
          val colsToValsProjects: Map[String, String] = Map("project_title" -> successForm.title,
            "project_lead" -> successForm.userName)
          val colsToValsNotes: Map[String, String] = Map("project_title" -> successForm.title,
            "note_title" -> s"${successForm.userName} created project.",
            "note_author" -> successForm.userName,
            "note_content" -> successForm.initialNote
          )
          try {
            val rowsInsertedProjects = DatabaseUpdate.insertInto("all_projects", colsToValsProjects)
            val rowsInsertedNotes = DatabaseUpdate.insertInto("project_notes", colsToValsNotes)
            if (rowsInsertedProjects == 1 && rowsInsertedNotes == 1) {
              Ok("Project Created")
            }
            else (BadRequest("Project Creation Failed"))
          } catch {
            case e: SQLException => e.printStackTrace; BadRequest("Project Creation Failed - SQL Error")
            case unknown => unknown.printStackTrace; BadRequest("Project Creation Failed - SQL Error")
          }
        }
      )
  }
}
