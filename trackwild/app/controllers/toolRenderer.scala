package controllers

import java.io.FileNotFoundException

import play.api.mvc._
import play.twirl.api.Html


/**
  * Created by nathanhanak on 7/17/17.
  */
object toolRenderer {

  def injectTemplateByName(templateName: String): play.twirl.api.Html = {
    templateName match {
      case "newDataUploader" => views.html.afterLogin.projectworkspace.newDataUploader()
      case "dataAppender" => views.html.afterLogin.projectworkspace.dataAppender()
      case _ => Html("")
    }
  }

}
