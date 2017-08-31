package models.database

import play.api.db.Database

/**
  * Object which is responsible for verifying the user's permission levels concerning access
  * to specific projects. The current levels are:
  *
  * 100 => "Project Lead: All permissions"
  * 200 => "Project Contributor: View all, add notes, upload data"
  * 250 => "Project Contributor: View all, add notes, no table data upload/addition"
  * 300 => "External Viewer: View all data - no write or upload"
  * 400 => "Public: View only"
  * 999 => "Forbidden Access"
  * _   => "Error: No permission set"
  *
  */
object ProjectPermissions {


  /**
    * Method which checks for existence of project via the title
    *
    * @param projectTitle the name of the project to lookup
    * @param db           the db where the project should be located
    * @return true if it is a valid project, false if not
    */
  def projectExists(projectTitle: String, db: Database): Boolean = {
    var tableExists: Boolean = false
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement("SELECT project_title FROM all_projects WHERE project_title=?")
      prepStmt.setString(1, projectTitle)
      val qryResult = prepStmt.executeQuery()
      if (qryResult.next()) {
        if (projectTitle.equals(qryResult.getString("project_title"))) tableExists = true
      }
      tableExists
    }
  }

  /**
    * Method verifies that user has the permission level high enough to access
    * the function for the project they are attempting with their HTTP request
    *
    * @param userName    the user attempting to access the project
    * @param maxLevelAllowed the minimum permission level required for the action
    * @return false if the user has a high enough permission, false if not.
    */
  def userHasPermissionLevel(userName: String, projectTitle: String, maxLevelAllowed: Int, db: Database): Boolean = {
    maxLevelAllowed >= getUserPermissionLevel(userName, projectTitle, db)
  }


  /**
    * Method returning the user's current permission levels for the project
    * @param userName the user name we are verifying
    * @param projectTitle the name of the project we are checking
    * @param db the current database containing the project
    * @return the permission level in an Int of the verified user
    */
  def getUserPermissionLevel(userName: String, projectTitle: String, db: Database): Int = {
    var permissionLevel = 999
    if (userIsProjectLead(userName, projectTitle, db)) permissionLevel = 100
    else {
      db.withConnection { conn =>
        val prepStmt = conn.prepareStatement("SELECT permission_level FROM collaborations " +
          "WHERE username=? AND project_title=?")
        prepStmt.setString(1, userName)
        prepStmt.setString(2, projectTitle)
        val qryResult = prepStmt.executeQuery()
        while (qryResult.next()) {
          permissionLevel = qryResult.getString("permission_level").toInt
        }
      }
    }
    permissionLevel
  }

  /**
    * Method which determines if the user passed into parameters is the project lead
    *
    * @param userName     the user in question
    * @param projectTitle the title of the project of which to find the user
    * @param db           the database containing the projects
    * @return true if the user is the lead of the project, false if not
    */
  def userIsProjectLead(userName: String, projectTitle: String, db: Database): Boolean = {
    var userIsLead = false
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement("SELECT project_lead FROM all_projects WHERE project_title=?")
      prepStmt.setString(1, projectTitle)
      val qryResult = prepStmt.executeQuery()
      if (qryResult.next()) {
        if (userName.equals(qryResult.getString("project_lead"))) userIsLead = true
      }
    }
    userIsLead
  }


}
