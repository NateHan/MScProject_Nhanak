package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneAppPerTest}
import play.api.db.Databases
import play.api.test.Helpers.{GET, OK, contentAsString, contentType, route, status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}

/**
  * Created by nathanhanak on 7/14/17.
  */
class LoginRegControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  val testDb = Databases(
    driver = "org.postgresql.Driver",
    url = "postgres://twadmin:trackwild@localhost:5432/track_wild_db"
  )

  "/login GET" should {

    "render the login page from a new instance of LoginRegController" in {
      val controller = new LoginRegController(testDb, stubControllerComponents())
      val loginPage = controller.loadLogin().apply(FakeRequest(GET, "/login"))

      status(loginPage) mustBe OK
      contentType(loginPage) mustBe Some("text/html")
      contentAsString(loginPage) must include ("Track Wild: Login")
    }

    "render the index page from the application" in {
      val controller = inject[LoginRegController]
      val loginPage = controller.loadLogin().apply(FakeRequest(GET, "/"))

      status(loginPage) mustBe OK
      contentType(loginPage) mustBe Some("text/html")
      contentAsString(loginPage) must include ("Track Wild: Login")
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val loginPage = route(app, request).get

      status(loginPage) mustBe OK
      contentType(loginPage) mustBe Some("text/html")
      contentAsString(loginPage) must include ("Track Wild: Login")
    }
  }

  "LoginRegController POST /login " should {

    val controller = new LoginRegController(testDb, stubControllerComponents())

    "return a BadRequest if user submits parameters in wrong format" in {
      controller.request
    }
  }


}
