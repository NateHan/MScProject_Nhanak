package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._

class AuthenticationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "AuthenticationController#sessionIsAuthenticated " should {

    "return true when given a session with authentication in the header " in {
      // test method directly
      val validRequest = FakeRequest(GET, "/dashboard")
        .withSession("authenticated" -> "true", "username" -> "testuser")
      val authController = new AuthenticationController(stubControllerComponents())

      authController.sessionIsAuthenticated(validRequest.session) mustBe true


      // testing by running through a controller
      val controller = new DashboardController(new AuthenticationController(stubControllerComponents()),
        stubControllerComponents())
      val dashRequest = controller.index().apply(FakeRequest(GET, "/dashboard")
        .withSession("authenticated" -> "true", "username" -> "testuser"))

      status(dashRequest) mustBe OK
      contentAsString(dashRequest) must include ("<h1>Project Dashboard</h1>")
    }

    "return false when given a session that has no authentication in the header" in {
      // test method directly
      val invalidRequest = FakeRequest(GET, "/dashboard")
        .withSession("authenticated" -> "false")
      val authController = new AuthenticationController(stubControllerComponents())

      authController.sessionIsAuthenticated(invalidRequest.session) mustBe false

      // testing by running through a controller
      val controller = new DashboardController(new AuthenticationController(stubControllerComponents()),
        stubControllerComponents())
      val badDashRequest= controller.index().apply(FakeRequest(GET, "/dashboard"))

      status(badDashRequest) mustBe UNAUTHORIZED
      contentAsString(badDashRequest) must include ("<h1>UNAUTHORIZED</h1>")
    }

  }

  "AuthenticationController#"

}
