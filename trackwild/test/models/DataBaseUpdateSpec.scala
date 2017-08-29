package models

import models.database.{DatabaseUpdate}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.Databases


class DataBaseUpdateSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {

  val twDB = Databases(
    driver = "org.postgresql.Driver",
    url = "postgres://twadmin:trackwild@localhost:5432/track_wild_db"
  )


  "DatabseUpdate object #insertInto " should {

    "return a value of 1 when inserting a single value " in {
      val tableName = "all_projects"
      val columnsAndValues = Map("project_title" -> "'Track Sharks 123456'", "project_lead" -> "'DemoUser'")

      DatabaseUpdate.insertRowInto(twDB, tableName, columnsAndValues) mustBe 1

      twDB.withConnection { conn =>
        val stmt = conn.createStatement
        val query = "DELETE FROM all_projects WHERE project_title='Track Sharks 848305439';"
        stmt.executeUpdate(query) mustBe 1
      }
    }

    "add ' ''s to an input value without ' ''s and return a value of 1 when inserting a single value" in {
      val tableName = "all_projects"
      val columnsAndValues = Map("project_title" -> "Track Dogs 123456", "project_lead" -> "DemoUser")

      DatabaseUpdate.insertRowInto(twDB, tableName, columnsAndValues) mustBe 1
      // remove test data
      twDB.withConnection { conn =>
        val stmt = conn.createStatement
        val query = "DELETE FROM all_projects WHERE project_title='Track Dogs 123456';"
        stmt.executeUpdate(query) mustBe 1
      }
    }
  }

}
