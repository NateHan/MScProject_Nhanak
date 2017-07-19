package controllers

import akka.util.Timeout
import scala.concurrent.duration._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers.{GET, OK, contentAsString, contentType, status}
import play.api.test.{FakeRequest, Injecting}

class ProjectWorkSpaceControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  implicit val duration: Timeout = 20 seconds
  val projectName = "TestProject"
  val controller = inject[ProjectsWorkSpaceController]

  "ProjectWorkSpaceController GET " should {

    "render the index page from the application" in {
      val workSpace = controller.loadWorkspace(projectName, "userName").apply(FakeRequest(GET, s"/projectworkspace/:userName/$projectName "))

      status(workSpace) mustBe OK
      contentType(workSpace) mustBe Some("text/html")
      contentAsString(workSpace) must include ("Track Wild: TestProject")
    }
  }

  "ProjectWorkSpaceController template renderer methods " should {
    //renderDataImporter, renderNewTableUploader, renderDataAppender, renderDataViewerTool
    "render the view dataImporterTool.scala.html with #renderDataImporter " in {
      val dataImportTool = controller.renderDataImporter().apply(FakeRequest(GET, "/projectworkspace/dataImporter"))

      status(dataImportTool) mustBe OK
      contentType(dataImportTool) mustBe Some("text/html")
    }

    "render the newDataUploader.scala.html template with #renderNewTableUploader" in {
      val newDataUploader = controller.renderNewTableUploader().apply(FakeRequest(GET, "/projectworkspace/datauploader"))

      status(newDataUploader) mustBe OK
      contentType(newDataUploader) mustBe Some("text/html")
    }

    "render the view dataAppender.scala.html with #renderDataAppender " in {
      val dataAppender = controller.renderDataAppender().apply(FakeRequest(GET, "/projectworkspace/dataAppender"))

      status(dataAppender) mustBe OK
      contentType(dataAppender) mustBe Some("text/html")
    }

    "render the view dataViewerTool.scala.html with #renderDataViewerTool" in {
      val dataViewer = controller.renderDataViewerTool().apply(FakeRequest(GET, "/projectworkspace/dataViewer"))

      status(dataViewer) mustBe OK
      contentType(dataViewer) mustBe Some("text/html")
    }

  }
}
