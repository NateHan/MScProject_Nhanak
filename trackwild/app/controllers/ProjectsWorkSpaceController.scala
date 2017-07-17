package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/16/17.
  */
class ProjectsWorkSpaceController @Inject() (cc: ControllerComponents) extends AbstractController(cc){

  def loadWorkspace(projectName:String, userName:String) = Action {
    implicit request: Request[AnyContent] => Ok(views.html.afterLogin
      .projectworkspace.projectView(projectName, userName))
      .withHeaders(SecurityHeadersFilter
      .CONTENT_SECURITY_POLICY_HEADER -> " .fontawesome.com .fonts.googleapis.com")
  }

}
