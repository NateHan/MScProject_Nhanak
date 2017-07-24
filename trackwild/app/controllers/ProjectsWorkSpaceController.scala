package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/16/17.
  */
class ProjectsWorkSpaceController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * loads main project workspace page
    * @param projectName the name of the current project of the user
    * @return an HTTP response containing the HTML for the project workspace
    */
  def loadWorkspace(projectName: String) = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.afterLogin
        .projectworkspace.projectView(projectName))
        .withHeaders(SecurityHeadersFilter
          .CONTENT_SECURITY_POLICY_HEADER -> " .fontawesome.com .fonts.googleapis.com")
  }

  /**
    * Loads the template which will allows the user to add new .csv or .xls data to their database
    * Will eventually nest a newDataUploader or dataAppender inside
    * @return an HTTP response containing the HTML for the table data uploader
    */
  def renderDataImporterSelector() = Action {
    implicit request: Request[AnyContent] => Ok(views.html.afterLogin.projectworkspace.dataImporterSelector())
  }


  /**
    * Loads the template which allows the user to add new .csv or .xls data to their database,
    * appending it to an already-existing table
    * @return an HTTP response containing the HTML for the table data appender
    */
  def renderDataAppender() = Action {
    implicit request: Request[AnyContent] => Ok(views.html.afterLogin.projectworkspace.dataAppender())
  }

  /**
    * Loads a template which will allow a user to select their data saved in the DB
    * and view it in the project workspace
    * @return
    */
  def renderDataViewerTool() = Action {
    implicit request: Request[AnyContent] => Ok(views.html.afterLogin.projectworkspace.dataViewerTool())
  }
}
