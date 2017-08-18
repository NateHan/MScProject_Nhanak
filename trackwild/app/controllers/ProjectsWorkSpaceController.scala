package controllers

import javax.inject.Inject
import javax.inject.Singleton

import models.adt.NoteObj
import models.database.{DataRetriever, DatabaseUpdate, ProjectNotesData, ProjectPermissions}
import models.formdata.{NewProjectData, NewProjectNote}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, text}
import play.api.db.Database
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/16/17.
  */
@Singleton
class ProjectsWorkSpaceController @Inject()(twDB: Database, authController: AuthenticationController, cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  /**
    * loads main project workspace page
    *
    * @param projectTitle the name of the current project of the user
    * @return an HTTP response containing the HTML for the project workspace
    */
  def loadWorkspace(projectTitle: String) = Action {
    implicit request: Request[AnyContent] =>
      if (ProjectPermissions.projectExists(projectTitle, twDB)) {
        val userName = request.session.get("username").getOrElse("No User Found in Session")
        if (ProjectPermissions.userHasPermissionLevel(userName, projectTitle, 400, twDB)) {
          val desiredPage = views.html.afterLogin.projectworkspace.projectView(projectTitle)
          authController.returnDesiredPageIfAuthenticated(request, desiredPage)
            .addingToSession("projectTitle" -> projectTitle)
        } else {
          Forbidden(views.html.afterLogin.projectworkspace.noPermissionFullPage())
        }
      } else {
        NotFound(views.html.afterLogin.projectworkspace.projectDoesNotExist(projectTitle))
      }
  }

  /**
    * Loads the template which will allows the user to add new .csv or .xls data to their database
    * Will eventually nest a newDataUploader or dataAppender inside
    *
    * @return an HTTP response containing the HTML for the table data uploader
    */
  def renderDataImporterSelector() = Action {
    implicit request: Request[AnyContent] =>
      val userName = request.session.get("username").getOrElse("No User Found in Session")
      val projectTitle = request.session.get("projectTitle").getOrElse("Project Not Found")
      if (ProjectPermissions.userHasPermissionLevel(userName, projectTitle, 249, twDB))
        Ok(views.html.afterLogin.projectworkspace.dataImporterSelector())
      else Ok(views.html.afterLogin.projectworkspace.noPermissionSmall())
  }


  /**
    * Loads the template which allows the user to add new .csv or .xls data to their database,
    * appending it to an already-existing table
    *
    * @return an HTTP response containing the HTML for the table data appender
    */
  def renderDataAppender() = Action {
    implicit request: Request[AnyContent] =>
      val userName = request.session.get("username").getOrElse("No User Found in Session")
      val projectTitle = request.session.get("projectTitle").getOrElse("Project Not Found")
      if (ProjectPermissions.userHasPermissionLevel(userName, projectTitle, 249, twDB))
        Ok(views.html.afterLogin.projectworkspace.dataAppender())
      else Ok(views.html.afterLogin.projectworkspace.noPermissionSmall())
  }

  /**
    * Loads a template which will allow a user to select their data saved in the DB
    * and view it in the project workspace
    *
    * @return
    */
  def renderDataPickerTool() = Action {
    implicit request: Request[AnyContent] =>
      val userName = request.session.get("username").getOrElse("No User Found in Session")
      val projectTitle = request.session.get("projectTitle").getOrElse("Project Not Found")
      if (ProjectPermissions.userHasPermissionLevel(userName, projectTitle, 401, twDB))
        authController.returnDesiredPageIfAuthenticated(
          request,
          views.html.afterLogin.projectworkspace.dataPickerTool(
            DataRetriever.retrieveAllProjectData(projectTitle, twDB)))
      else Ok(views.html.afterLogin.projectworkspace.noPermissionSmall())
  }

  /**
    * Loads a template containing all the notes for a project which will load in the project workspace
    *
    * @param projectTitle the name of the current project
    * @return the view which will contain all of the notes for the project
    */
  def getAllNotes(projectTitle: String) = Action {
    implicit request: Request[AnyContent] =>
      val userName = request.session.get("username").getOrElse("No User Found in Session")
      if (ProjectPermissions.userHasPermissionLevel(userName, projectTitle, 399, twDB)) {
        val allProjectNotes: List[NoteObj] = ProjectNotesData.getAllProjectNotes(projectTitle, twDB)
        Ok(views.html.afterLogin.projectworkspace.projectNotes(allProjectNotes))
      } else {
        Ok(views.html.afterLogin.projectworkspace.noPermissionSmall())
      }
  }

  /**
    * Method which retrieves a view which will contain the desired table for the project by name
    *
    * @param tableName the SQL formatted name of the data table we are tryign to retrieve
    * @return an HTML view containing the table in a viewable format for the project workspace
    */
  def renderProjectDataTable(tableName: String) = Action {
    implicit request: Request[AnyContent] =>
      val userName = request.session.get("username").getOrElse("No User Found in Session")
      val project = request.session.get("projectTitle").getOrElse("No Project Title Found in Session")
      if (ProjectPermissions.userHasPermissionLevel(userName, project, 399, twDB)) {
        val fullTable = DataRetriever.retrieveFullDataTableByName(tableName, twDB)
        Ok(views.html.afterLogin.projectworkspace.tableBoxProjDataWorkspace(fullTable))
      } else {
        Ok(views.html.afterLogin.projectworkspace.noPermissionSmall())
      }
  }

  /**
    * Method which will load the form allowing the user to append a note to the current project
    *
    * @return an Ok request containing the view template with the form to enter a new note
    */
  def renderNoteAdder() = Action {
    implicit request: Request[AnyContent] =>
      if (authController.userHasRequiredPermissionLevel(299, request) && authController.sessionIsAuthenticated(request.session)) {
        Ok(views.html.afterLogin.projectworkspace.newNotePostForm(newProjNote))
      } else {
        Unauthorized(views.html.afterLogin.projectworkspace.noPermissionSmall())
      }
  }


  val newProjNote: Form[NewProjectNote] = Form {
    mapping(
      "projectTitle" -> nonEmptyText,
      "noteTitle" -> text,
      "noteAuthor" -> nonEmptyText,
      "noteContent" -> nonEmptyText,
    )(NewProjectNote.apply)(NewProjectNote.unapply)
  }

  /**
    * Method which turns user input for a note into a entry into the DB for notes for the project
    *
    * @return an Action result based on if the user is authorized, has permission, or is successful
    */
  def postNewNoteToDb() = Action { implicit request: Request[AnyContent] =>
    if (authController.sessionIsAuthenticated(request.session)) {
      newProjNote.bindFromRequest().fold(
        errorForm => BadRequest("Form data did not bind"),
        successForm => {
          val columnsTovals: Map[String, String] = mapSQLColumnLabelsToNoteFormFields(successForm)
          if (DatabaseUpdate.insertRowInto(twDB, "project_notes", columnsTovals) == 1) Ok(views.html.afterLogin.projectworkspace.noteAddSuccess())
          else {
            BadRequest("Unable to insert note")
          }
        }
      )
    } else {
      Unauthorized(views.html.expiredSession("You have been logged out or your session has expired"))
    }
  }


  /**
    * Take the fields collected from form and place into values for a map where the SQL table
    * column names are the keys
    * @param note         the new note data type containing the note fields
    * @return a Map of keys:SQL Columns to values: form fields
    */
  def mapSQLColumnLabelsToNoteFormFields(note: NewProjectNote): Map[String, String] = {
    Map(
      "project_title" -> note.projectTitle,
      "note_title" -> note.noteTitle,
      "note_author" -> note.noteAuthor,
      "note_content" -> note.noteContent
    )
  }

}
