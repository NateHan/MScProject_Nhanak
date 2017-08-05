package models.database

import play.api.db.Database

import scala.collection.mutable.ListBuffer

object ProjectsUserLeads {

  /**
    * Retrieves a list of all the projects the user currently leads. The result is
    * a list of maps where the keys represent the column names from the table
    *
    * @param userName the current user
    * @param db       the default database for the application
    * @return the List of Maps of rows from the resulting PSQL query
    */
  def getAll(userName: String, db: Database): List[Array[String]] = {
    val tablesQuery = s"SELECT project_title, created_date, isActive FROM all_projects WHERE project_lead=? ORDER BY created_date DESC;"
    var returnResult = new ListBuffer[Array[String]]
    db.withConnection { conn =>
      val ps = conn.prepareStatement(tablesQuery)
      ps.setString(1, userName)
      val qryResult = ps.executeQuery()
      val columnCount = qryResult.getMetaData.getColumnCount
      while (qryResult.next()) {
        val row = new Array[String](columnCount)
        for (i <- 1 to columnCount) {
          row(i-1) = qryResult.getString(i)
        }
        returnResult += row
      }
    }
    returnResult.toList
  }

}
