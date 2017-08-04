package models.database

import play.api.db.Database

import scala.collection.mutable.ListBuffer

object ProjectsUserLeads {

  /**
    * Retrieves a list of projects the user currently leads. The result is
    * a list of maps where the keys represent the column names from the table
    * @param userName the current user
    * @param db the default database for the application
    * @return the List of Maps of rows from the resulting PSQL query
    */
  def getAll(userName:String, db: Database): List[Array[String]] = {
    val query = s"SELECT project_title, created_date, isActive FROM all_projects WHERE project_lead='$userName' ORDER BY created_date DESC;"
    db.withConnection{ conn =>
      val stmt = conn.createStatement()
      val qryResult = stmt.executeQuery(query)
      val resMetaData = qryResult.getMetaData
      var returnResult = new ListBuffer[Array[String]]
      while (qryResult.next()) {
        for (i <- 1 to resMetaData.getColumnCount ) {

        }
      }
    }
    List(Array[String]())
  }

}
