package models

import models.database.{ProjectPermissions, TrackWildDatabaseGrabber}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class ProjectPermissionsSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {

  val twDB = TrackWildDatabaseGrabber.getApplicationDataBase
  val validProjectTitle = "testProject"

  override def beforeAll(): Unit = {
    twDB.withConnection { conn =>
      val stmt = conn.createStatement()
      stmt.executeUpdate(s"INSERT INTO all_projects( project_title, project_lead) VALUES ('$validProjectTitle', 'DemoUser');")
    }
  }

  override def afterAll(): Unit = {
    twDB.withConnection { conn =>
      val stmt = conn.createStatement()
      stmt.executeUpdate(s"DELETE FROM all_projects WHERE project_title='$validProjectTitle';")
    }
  }


  "ProjectPermissions#projectExists " should {

    "Return true when passed a project title which exists" in {

      ProjectPermissions.projectExists(s"$validProjectTitle", twDB) mustBe true
    }

    "Return false when passed a project title which does not exist " in {

      ProjectPermissions.projectExists("Not a real project", twDB) mustBe false
    }

  }

}
