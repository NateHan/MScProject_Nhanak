package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.db.Databases
import play.api.mvc.{Action, Result}
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._

class AuthenticationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val testDb = Databases(
    driver = "org.postgresql.Driver",
    url = "postgres://twadmin:trackwild@localhost:5432/track_wild_db"
  )

  "AuthenticationController#sessionIsAuthenticated " should {

    "return true when given a session with authentication in the header " in {
      // test method directly
      val validRequest = FakeRequest(GET, "/dashboard")
        .withSession("authenticated" -> "true", "username" -> "testuser")
      val authController = inject[AuthenticationController]

      authController.sessionIsAuthenticated(validRequest.session) mustBe true


      // testing by running through a controller
      //twDB: Database, authController: AuthenticationController, cc: ControllerComponents
      val controller = inject[DashboardController]
      val dashRequest = controller.index().apply(FakeRequest(GET, "/dashboard")
        .withSession("authenticated" -> "true", "username" -> "testuser"))

      status(dashRequest) mustBe OK
      contentAsString(dashRequest) must include ("<h1>PROJECT DASHBOARD</h1>")
    }

    "return false when given a session that has no authentication in the header" in {
      // test method directly
      val invalidRequest = FakeRequest(GET, "/dashboard")
        .withSession("authenticated" -> "false")
      val authController = new AuthenticationController(stubControllerComponents(), testDb)

      authController.sessionIsAuthenticated(invalidRequest.session) mustBe false

      // testing by running through a controller
      val controller = inject[DashboardController]
      val badDashRequest= controller.index().apply(FakeRequest(GET, "/dashboard"))

      status(badDashRequest) mustBe UNAUTHORIZED
      contentAsString(badDashRequest) must include ("<h1>UNAUTHORIZED</h1>")
    }

  }

  "AuthenticationController#returnDesiredPageIfAuthenticated " should {

    "return an Ok Result containing the expected view in an authorized session" in {
      implicit val validRequest = FakeRequest(GET, "/dashboard")
        .withSession("authenticated" -> "true", "username" -> "testuser")
      val authController = new AuthenticationController(stubControllerComponents(), testDb)

      val result = authController.returnDesiredPageIfAuthenticated(validRequest, views.html.afterLogin.dashboardviews.dashboard())

      result.header.status mustBe 200
    }

    "return an unauthorized Result containing the invalidSession.html page for an unauthorized session" in {
      implicit val invalidRequest = FakeRequest(GET, "/dashboard")
      val authController = new AuthenticationController(stubControllerComponents(), testDb)

      val result = authController.returnDesiredPageIfAuthenticated(invalidRequest, views.html.afterLogin.dashboardviews.dashboard())

      result.header.status mustBe 401
    }
  }

}
