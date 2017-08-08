package controllers

import javax.inject.Inject
import javax.inject.Singleton

import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter
import play.twirl.api.Html

@Singleton
class AuthenticationController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

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



}

