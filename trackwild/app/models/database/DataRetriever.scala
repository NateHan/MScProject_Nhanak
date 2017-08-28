package models.database

import java.sql.ResultSet

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
        for (i <- 0 to noOfCols - 1) {
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
    var message = "Cannot run your query because it contains SQL which is not allowed: " + returnIllegalSQL(qry.query)
    val resultBuilder = new ListBuffer[Array[String]]
    if (message.contains("none")) {
      db.withConnection { conn =>
        val stmt = conn.createStatement()
        try {
          val qryResult = stmt.executeQuery(qry.query)
          val resMetaData = qryResult.getMetaData
          resultBuilder += getColumnLabelsFromResultSet(qryResult)
          while (qryResult.next()) {
            for (i <- 1 to resMetaData.getColumnCount) {

            }
          }
          (resultBuilder.toList, message)
        } catch {

        }
      }
    } else
      (resultBuilder.toList, message)
  }

  // check query for illegal strings
  //    if clean: Execute query, return the result, make up a message
  //    if not clean, return an empty List, put what went wrong in the ErrorMessage

  /**
    * Checks to see if the incoming query string is strictly a SELECT statement and
    * is niether destructive or additive
    *
    * @param qry the incoming query
    * @return "clean" if the String is legal, an error message if it contains bad SQL.
    */
  private def returnIllegalSQL(qry: String): String = {
    val illegalKeys = List("DELETE", "UPDATE", "INSERT", "DROP", "CREATE")
    var illegalKeyInQuery = "none"
    for (key <- illegalKeys) {
      if (qry.toUpperCase.contains(key)) illegalKeyInQuery = key
    }
    illegalKeyInQuery
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
    for (i <- 1 to resMetaData.getColumnCount) {
      headerRow(i - 1) = resMetaData.getColumnName(i)
    }
    headerRow
  }
}
