package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by nathanhanak on 7/14/17.
  */
@Singleton
class DashboardController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  def index() = Action {
    implicit request: Request[AnyContent] => Ok(views.html.success()).withHeaders(SecurityHeadersFilter
      .CONTENT_SECURITY_POLICY_HEADER -> " .fontawesome.com .fonts.googleapis.com")
  }
}
