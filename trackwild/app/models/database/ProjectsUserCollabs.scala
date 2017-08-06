package models.database

import play.api.db.Database

import scala.collection.mutable.ListBuffer

object ProjectsUserCollabs {

  /**
    * Method which retrieves a list of projects which the user collaborates on from the database
    * @param userName the current session's active user
    * @param db the default database for the application
    * @return a list of the rows retrieved from the DB detailing the user's collaborated projects
    */
  def getAllCollabs(userName: String, db: Database): List[Array[String]] = {
    val tableQuery = "SELECT c.project_title, ap.project_lead, c.permission_level, ap.isActive " +
      "FROM (SELECT project_title, permission_level FROM collaborations WHERE username=?) c JOIN all_projects ap " +
      "ON c.project_title = ap.project_title"
    val returnResult = new ListBuffer[Array[String]]
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement(tableQuery)
      prepStmt.setString(1, userName)
      val qryResult = prepStmt.executeQuery()
      val columnCount = qryResult.getMetaData.getColumnCount
      while (qryResult.next()) {
        val row = new Array[String](columnCount)
        for (i <- 1 to columnCount) {
          row(i - 1) = qryResult.getString(i)
        }
        returnResult += row
      }
    }
    returnResult.foreach( row => row(2) = replaceIntWithDescription(row(2).toInt)) // replaces the INT level with a description
    returnResult.toList
  }


  /**
    * Method which maps the integer of the user's permission level to a readable description of the permission
    * @param permissionLevel the database level information of user's permission
    * @return a readable descriptive translation of their allowed permissions.
    */
  def replaceIntWithDescription(permissionLevel: Int): String = permissionLevel match{
    case 100 => "Project Lead: All permissions"
    case 200 => "Project Contributor: View all, add notes, upload data"
    case 250 => "Project Contributor: View all, add notes, no data upload"
    case 300 => "External Viewer: View all data - no write or upload"
    case 400 => "Public: View only"
    case _ => "Error: No permission set"
  }
}
