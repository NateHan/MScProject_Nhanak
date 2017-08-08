package models.database

import play.api.db.Database

object ProjectPermissions {


  /**
    * Method which checks for existence of project
    * @param projectTitle the name of the project to lookup
    * @param db the db where the project should be located
    * @return true if it is a valid project, false if not
    */
  def projectExists(projectTitle: String, db: Database): Boolean ={
    var tableExists: Boolean = false
    db.withConnection{ conn =>
      val prepStmt = conn.prepareStatement("SELECT project_title FROM all_projects WHERE project_title=?")
      prepStmt.setString(1, projectTitle)
      val qryResult = prepStmt.executeQuery()
      if (qryResult.next()) {
        if (projectTitle.equals(qryResult.getString("project_title"))) tableExists = true
      }
      tableExists
    }
  }

}
