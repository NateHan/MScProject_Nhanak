package controllers

import java.nio.file.Paths
import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents, Request}

/**
  * class which controls the upload of new data files
  *
  * @param authController a controller which handles the verification of the authentication of the session
  * @param cc             controller components
  */
class DataUploadController @Inject()(authController: AuthenticationController, cc: ControllerComponents) extends AbstractController(cc) {



  def uploadNewTable = Action(parse.multipartFormData) {implicit request =>
    request.body.file("fileUpload").map { dataFile =>
      if (authController.sessionIsAuthenticated(request.session)) {
        val filename = dataFile.filename
        if(filename.endsWith(".csv") || filename.endsWith(".xls")) {
          dataFile.ref.atomicMoveWithFallback(Paths.get(s"/public/tmp/$filename")) //may have to change this path to include username in the future
          Ok(views.html.afterLogin.projectworkspace.projectView("REPLACE PROJECT NAME"))
        } else NotAcceptable("File Was not in .csv or .xls format")
      } else Unauthorized(views.html.invalidSession("Your session expired or you logged out"))
    }getOrElse( NotFound("Something Happened Along the way"))
  }

}
