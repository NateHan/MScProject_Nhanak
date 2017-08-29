package models

import models.database.{ProjectPermissions}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.Databases

class ProjectPermissionsSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {

  val twDB = Databases(
    driver = "org.postgresql.Driver",
    url = "postgres://twadmin:trackwild@localhost:5432/track_wild_db"
  )

  val validProjectTitle = "testProject"
  val lowLevelUser = "testUser"

  override def beforeAll(): Unit = {
    twDB.withConnection { conn =>
      val stmt = conn.createStatement()
      stmt.executeUpdate(s"INSERT INTO all_projects( project_title, project_lead) VALUES ('$validProjectTitle', 'DemoUser');")

      stmt.executeUpdate(s"INSERT INTO collaborations(username, project_title, permission_level) VALUES " +
        s"('nhanak', '$validProjectTitle', 200) ")
      stmt.executeUpdate(s"INSERT INTO verified_users(uemail, upassword, username, fullname) " +
        s"VALUES ('test@test.com', 'testerpass', '$lowLevelUser', 'test user')")
      stmt.executeUpdate(s"INSERT INTO collaborations(username, project_title, permission_level) VALUES " +
        s"('$lowLevelUser', '$validProjectTitle', 300)")

    }
  }

  override def afterAll(): Unit = {
    twDB.withConnection { conn =>
      val stmt = conn.createStatement()
      stmt.executeUpdate(s"DELETE FROM collaborations WHERE project_title='$validProjectTitle'")
      stmt.executeUpdate(s"DELETE FROM all_projects WHERE project_title='$validProjectTitle';")
      stmt.executeUpdate(s"DELETE FROM verified_users where username='$lowLevelUser'")
    }
  }


  "ProjectPermissions#projectExists " should {

    "return true when passed a project title which exists" in {

      ProjectPermissions.projectExists(s"$validProjectTitle", twDB) mustBe true
    }

    "return false when passed a project title which does not exist " in {

      ProjectPermissions.projectExists("Not a real project", twDB) mustBe false
    }

  }

  "ProjectPermissions#userHasPermissionLevel " should {

    "return true when a given user has the expected permission level for a project " in {
      ProjectPermissions.userHasPermissionLevel("nhanak", validProjectTitle, 200, twDB) mustBe true
    }

    "return true when a given user is the project leader for the project " in {
      ProjectPermissions.userHasPermissionLevel("DemoUser", validProjectTitle, 200, twDB) mustBe true
    }

    "return false when a given user does not have the expected permission level for a project " in {
      ProjectPermissions.userHasPermissionLevel(lowLevelUser, validProjectTitle, 200, twDB) mustBe false
    }

  }

}
