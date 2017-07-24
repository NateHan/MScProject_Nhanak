package controllers

import java.io.File
import java.nio.file.Paths
import javax.inject.Inject

import models.formdata.NewDataTableInfo
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

/**
  * class which controls the upload of new data files
  *
  * @param authController a controller which handles the verification of the authentication of the session
  * @param cc             controller components
  */
class DataUploadController @Inject()(authController: AuthenticationController, cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  /**
    * Loads the template which allows the user to load a new table in .csv or .xls
    * form and add it to their database
    * @return an HTTP response containing the HTML for the table uploader
    */
  def renderNewTableUploader() = Action {
    implicit request: Request[AnyContent] => Ok(views.html.afterLogin.projectworkspace.newDataUploader(tableUploadForm))
  }

  val tableUploadForm = Form(
    mapping(
      "tableName" -> nonEmptyText,
      "uploadingUser" -> nonEmptyText
    )(NewDataTableInfo.apply)(NewDataTableInfo.unapply)
  )

  /**
    * Allows user to upload a new table to their project
    * Checks if authenticated
    * Checks if the file is in the correct format
    * Triggers creation of a new table in the DB and transfers data
    * Maybe just have it flash an error if it doesn't work?
    * @return
    */
  def uploadNewTable = Action(parse.multipartFormData) {implicit request =>
    request.body.file("fileUpload").map { dataFile =>
      if (authController.sessionIsAuthenticated(request.session)) {
        val filename = dataFile.filename
        val saveToPath: String = s"/Users/nathanhanak/GithubRepos/MScProject_Nhanak/trackwild/public/tmp/$filename"
        if(filename.endsWith(".csv") || filename.endsWith(".xls")) {
          dataFile.ref.moveTo(new File(saveToPath))
          Ok(views.html.afterLogin.projectworkspace.projectView("REPLACE PROJECT NAME"))
        } else NotAcceptable("File Was not in .csv or .xls format")
      } else Unauthorized(views.html.invalidSession("Your session expired or you logged out"))
    }getOrElse( NotFound("Something Happened Along the way"))
  }


}
