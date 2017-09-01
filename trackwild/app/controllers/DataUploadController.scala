package controllers

import java.io.File
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

import models.formdata.NewDataTableInfo
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import models.database.{CSVFileToDBParser, FileToDBParser}
import play.api.db.Database
import play.filters.headers.SecurityHeadersFilter

/**
  * class which controls the upload of new data files
  *
  * @param authController a controller which handles the verification of the authentication of the session
  * @param cc             controller components
  */
@Singleton
class DataUploadController @Inject()(csvFileToDBParser: CSVFileToDBParser, twDB: Database,
                                     authController: AuthenticationController,
                                     cc: ControllerComponents)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  /**
    * Loads the template which allows the user to load a new table in .csv or .xls
    * form and add it to their database3
    * @return an HTTP response containing the HTML for the table uploader
    */
  def renderNewTableUploader() = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.afterLogin.projectworkspace.newDataUploader(tableUploadForm))
  }

  val tableUploadForm = Form(
    mapping(
      "tableName" -> nonEmptyText,
      "uploadingUser" -> nonEmptyText,
      "projectTitle" -> nonEmptyText
    )(NewDataTableInfo.apply)(NewDataTableInfo.unapply)
  )

  /**
    * Allows user to upload a new table to their project
    * Checks if authenticated
    * Triggers creation of a new table in the DB and transfers data
    * @return
    */
  def uploadNewTable() = Action(parse.multipartFormData) {implicit request =>
    val newTableForm : Option[NewDataTableInfo] = tableUploadForm.bindFromRequest().fold(
      errorForm => None,
      successForm => Some(successForm)
    )
    request.body.file("fileUpload").map { dataFile =>
      if (authController.sessionIsAuthenticated(request.session)) {
        val saveToPath: String = s"/Users/nathanhanak/GithubRepos/MScProject_Nhanak/trackwild/public/tmp/${dataFile.filename}"
        val file = dataFile.ref.moveTo(new File(saveToPath))
        csvFileToDBParser.parseFileToNewRelation(file,
          Map( "userName" -> newTableForm.get.uploadingUser,
            "tableName" -> newTableForm.get.tableName,
          "projectTitle" -> newTableForm.get.projectTitle))
        Ok(views.html.afterLogin.projectworkspace.projectView(request.session.get("projectTitle").getOrElse("Project Not Found")))
          .withHeaders(SecurityHeadersFilter
          .CONTENT_SECURITY_POLICY_HEADER ->
            " .fontawesome.com .fonts.googleapis.com maps.googleapis.com csi.gstatic.com")
      } else Unauthorized(views.html.invalidSession("Your session expired or you logged out"))
    }getOrElse( NotFound("Something Happened Along the way"))
  }


}
