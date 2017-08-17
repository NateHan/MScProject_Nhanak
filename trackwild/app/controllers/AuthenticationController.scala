package controllers

import javax.inject.Inject
import javax.inject.Singleton

import models.database.ProjectPermissions
import play.api.db.Database
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter
import play.twirl.api.Html

@Singleton
class AuthenticationController @Inject()(cc: ControllerComponents, twDB:Database) extends AbstractController(cc) {

  /**
    * Searches for authentication parameter set in session's header cookie
    *
    * @param session the current session
    * @return true if authentication found, false if not
    */
  def sessionIsAuthenticated(session: Session): Boolean = session.get("authenticated") match {
    case Some(value) if value.equals("true") => true
    case _ => false
  }

  /**
    * Checks request header cookie to see if user has been authenticated. Renders desired
    * views.html page if successful, returns an Unathorized response if not found.
    * @param request the HTTP request sent by user
    * @param successPage the desired Html page to render if authenticated
    * @param errorMessage message to load into invalidSession view if not successful
    * @return Action containing the response of the operation
    */
  def returnDesiredPageIfAuthenticated(request: Request[AnyContent], successPage: Html,
                                       errorMessage: String = "Track Wild Msg: Must be logged in to access"):Result = {
      request.session.get("authenticated") match {
        case Some(value) if value.equals("true") => Ok(successPage).withHeaders(SecurityHeadersFilter
          .CONTENT_SECURITY_POLICY_HEADER -> " .fontawesome.com .fonts.googleapis.com")
        case _ => Unauthorized(views.html.invalidSession(errorMessage)(request)).withNewSession
      }
    }

  /**
    * Method which checks the user's permission level for the current project
    * @param requiredLevel the highest level allowed for the desired action from the request
    * @param request the current GET or POST request made from the client
    * @return true if the user has a low enough permission level, false if not
    */
  def userHasRequiredPermissionLevel(requiredLevel:Int, request: Request[AnyContent]): Boolean = {
    val userName = request.session.get("username").getOrElse("No User Found in Session")
    val projectTitle = request.session.get("projectTitle").getOrElse("No Project Title Found in Session")
    ProjectPermissions.userHasPermissionLevel(userName, projectTitle, requiredLevel, twDB)
  }




}

