package models.database

import java.sql.{ResultSet, SQLException}

import models.formdata.TableSQLScript
import play.api.db.Database

import scala.collection.mutable.ListBuffer

object DataRetriever {

  /**
    * Method which will retrieve all of the data tables available to a specific project
    * Each array of the List will be in this format:
    * | Table Name User format | Created by(userName) | latest Update | Latest Updating User | SQL table name
    *
    * @param projectTitle the name of the project containing the data we need
    * @param db           the current DB of the application where the project is stored
    * @return a list of rows , each representing a different table name.
    */
  def retrieveAllProjectData(projectTitle: String, db: Database): List[Array[String]] = {
    val tableCollector = new ListBuffer[Array[String]]
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement("SELECT userview_table_name, sql_table_name FROM all_data_tables WHERE project_title=?")
      prepStmt.setString(1, projectTitle)
      val allTablesRslt = prepStmt.executeQuery()
      while (allTablesRslt.next()) {
        val sqlTableName = allTablesRslt.getString("sql_table_name")
        val (lastUpdate, byUser) = retrieveLatestUpdateInfo(sqlTableName, db)
        tableCollector += Array(
          allTablesRslt.getString("userview_table_name"),
          retrieveTableCreator(sqlTableName, db),
          lastUpdate,
          byUser,
          sqlTableName)
      }
    }
    tableCollector.toList
  }

  /**
    * Runs a query to determine the oldest entry into a table and get the user responsible.
    *
    * @param tableName the name of the table we are querying
    * @param db        the database containing the table
    * @return the username as a String responsible for the oldest entry.
    */
  def retrieveTableCreator(tableName: String, db: Database): String = {
    var userName = ""
    db.withConnection { conn =>
      val query = s"SELECT uploaded_by, date_added FROM $tableName WHERE date_added = " +
        s"(SELECT MIN(date_added) FROM $tableName);"
      val stmt = conn.createStatement()
      val qryResult = stmt.executeQuery(query)
      if (qryResult.next()) {
        userName = qryResult.getString("uploaded_by")
      }
    }
    userName
  }

  /**
    * Method which retrieves the latest update to the table and user who was responsible
    *
    * @param tableName the SQL name of the table we are looking for
    * @param db        the DB location of the table
    * @return a tuple containing (latestUpdate, theUserResponsible)
    */
  def retrieveLatestUpdateInfo(tableName: String, db: Database): (String, String) = {
    var result = ("", "")
    db.withConnection { conn =>
      val stmt = conn.createStatement
      val qry = s"SELECT date_added, uploaded_by FROM $tableName WHERE date_added = " +
        s"(SELECT MAX(date_added) FROM $tableName);"
      val qryResult = stmt.executeQuery(qry)
      if (qryResult.next()) {
        result = (qryResult.getString("date_added"), qryResult.getString("uploaded_by"))
      }
    }
    result
  }

  /**
    * Method which will retrieve a project's data table by its name.
    *
    * @param tableName the name of the data table we would like to retrieve
    * @param db        the database where the table is located
    * @return The table as a list of Arrays. The first row will always be the header.
    */
  def retrieveFullDataTableByName(tableName: String, db: Database): List[Array[String]] = {
    val resultBuffer = new ListBuffer[Array[String]]
    resultBuffer += getTableheaders(tableName, db)
    val noOfCols = resultBuffer.head.length
    db.withConnection { conn =>
      val stmt = conn.createStatement()
      val resultSet = stmt.executeQuery(s"SELECT * FROM $tableName;")
      while (resultSet.next()) {
        val row = new Array[String](noOfCols)
        for (i <- 0 until noOfCols) {
          row(i) = resultSet.getString(resultBuffer.head(i)) // retrieves each row's entry by column name
        }
        resultBuffer += row
      }
    }
    resultBuffer.toList
  }

  /**
    * Method which retrieves only the column titles of the desired table
    *
    * @param tableName the name of the table we are interested in
    * @param db        the database location of the table
    * @return an array containing one entry for each column label
    */
  def getTableheaders(tableName: String, db: Database): Array[String] = {
    db.withConnection { conn =>
      val stmt = conn.createStatement()
      val resSet = stmt.executeQuery(s"SELECT * FROM $tableName WHERE false;")
      val rsMetaData = resSet.getMetaData
      val headerRow: Array[String] = new Array[String](rsMetaData.getColumnCount)
      for (i <- 1 to rsMetaData.getColumnCount) {
        headerRow(i - 1) = rsMetaData.getColumnName(i)
      }
      headerRow
    }
  }

  /**
    * Performs a query on a view, collected from the client-side form in the tableQueryFormView
    *
    * @param qry TableSQLScript containing the name of the view and the query text
    * @param db  the db location of the current view
    * @return List of an Array of Strings containing the returned result of a successful SQL
    *         query (the first row contains the headers, the other rows contain the results),
    *         or an empty List and an error message if an error happened.
    */
  def performQueryOnView(qry: TableSQLScript, db: Database): (List[Array[String]], String) = {
    var message = returnIllegalSQLifPresent(qry)
    val resultBuilder = new ListBuffer[Array[String]]
    if (message.equals("clean query")) {
      db.withConnection { conn =>
        val stmt = conn.createStatement()
        try { // needs to handle SQL exceptions if entered SQL has error
          val qryResult = stmt.executeQuery(qry.query)
          val resMetaData = qryResult.getMetaData
          resultBuilder += getColumnLabelsFromResultSet(qryResult)
          var i = 0
          while (qryResult.next()) {
            i = i+1
            val tableRow = new Array[String](resMetaData.getColumnCount)
            for (i <- 0 until resMetaData.getColumnCount) {
              tableRow(i) =  qryResult.getString(resultBuilder.head(i))
            }
            resultBuilder += tableRow
          }
        } catch {
          case e:Exception =>
            message = e.getMessage
            e.printStackTrace();
        }
      }
    }
      (resultBuilder.toList, message)
  }

  /**
    * Checks to see if the incoming query string is strictly a SELECT statement and
    * is neither destructive or additive
    *
    * @param qryObj› the incoming query wrapped in a case class
    * @return "clean" if the String is legal, an error message if it contains bad SQL.
    */
  private def returnIllegalSQLifPresent(qryObj: TableSQLScript): String = {
    val illegalKeys = List("DELETE", "UPDATE", "INSERT", "DROP", "CREATE")
    var queryStatus = "clean query"
    for (key <- illegalKeys) {
      if (qryObj.query.toUpperCase.contains(key)) queryStatus = "Cannot run your query, " +
        "it contains SQL which is not allowed: " + key
    }
    val requiredFromStmt = s"FROM ${qryObj.viewName}"
    if (!qryObj.query.toUpperCase.contains(requiredFromStmt.toUpperCase())) queryStatus =
      s"""Query must contain '$requiredFromStmt'"""
    queryStatus
  }

  /**
    * Method returns all the column name labels from a succesfully executed ResultSet
    *
    * @param resultSet the succesfully executed result Set
    * @return an Array of Column Labels in String format
    */
  private def getColumnLabelsFromResultSet(resultSet: ResultSet): Array[String] = {
    val resMetaData = resultSet.getMetaData
    val headerRow: Array[String] = new Array[String](resMetaData.getColumnCount)
    for (i <- 1 to headerRow.length) {
      headerRow(i - 1) = resMetaData.getColumnName(i)
    }
    headerRow
  }

  /**
    * Method which retrieves the collaborators and their permission levels for the current project
    * @param projectTitle THe project title we are seeking collaborators for.
    * @param db the database where the project information is stored
    * @return a List of table rows, each stored as an Array[String]
    */
  def retrieveCollaboratorsForProject(projectTitle:String, db:Database): List[Array[String]] = {
    val resultTable = new ListBuffer[Array[String]]
    db.withConnection { conn =>
      val stmt = conn.createStatement()
      val resultSet = stmt.executeQuery(s"SELECT username, permission_level FROM collaborations WHERE project_title='$projectTitle';")
      while (resultSet.next) {
        val row = new Array[String](2)
        row(0) = resultSet.getString(1)
        row(1) = resultSet.getInt(2).toString
        resultTable += row
      }
      resultTable.toList
    }
  }

  /**
    * Checks if user entered into collaboration form is a valid user
    * @param userHandle the moniker entered by the user to search for a user
    * @param db the database where the data is located
    * @return true if the user moniker is found in verified_users, false if not.
    */
  def userExists(userHandle:String, db:Database): Boolean = {
    var userExists = false
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement("SELECT username, uemail FROM verified_users WHERE username=? OR uemail=?")
      prepStmt.setString(1, userHandle)
      prepStmt.setString(2, userHandle)
      val resultSet= prepStmt.executeQuery()
      if (resultSet.next()) {
        if (userHandle.equals(resultSet.getString("username")) ||
          userHandle.equals(resultSet.getString("uemail"))) userExists = true
      }
    }
    userExists
  }


}
