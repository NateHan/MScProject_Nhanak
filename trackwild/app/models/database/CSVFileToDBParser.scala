package models.database

import java.io.File

import scala.io.{BufferedSource, Source}
import java.sql.{Connection, ResultSet, SQLException, Statement}
import javax.inject.Inject
import javax.inject.Singleton

import models.database.SQLStringFormatter
import com.github.tototoshi.csv.CSVReader
import play.api.db.Database

@Singleton
class CSVFileToDBParser @Inject()(twDB: Database) extends FileToDBParser {


  /**
    * Method which will create a new relation from user-uploaded data.
    * @param file the file containing the data to be inserted
    * @param tableInfo info about PSQL table in the database from the request.
    *                  Keys are "userName", "tableName", and "projectTitle"
    * @return True if the operation was successfully able to add data to a relation
    */
  override def parseFileToNewRelation(file: File, tableInfo: Map[String,String]): Boolean = {
    var parseSuccess = false
    twDB.withConnection { conn : Connection =>
      val stmt = conn.createStatement()
      val tableNameSQLFormat = SQLStringFormatter.returnStringInSQLNameFormat(tableInfo.getOrElse("tableName", "Unspecified Table Name"))
      val finalTableName = returnUniqueTableName(tableNameSQLFormat, conn)
      try {
        createNewTable(file, finalTableName, stmt)
        addNewTableToAllDataTables(tableInfo.getOrElse("tableName", "Unspecified Table Name"), finalTableName, tableInfo.getOrElse("projectTitle", "No project found"))
        parseSuccess = addDataRowsToTable(file, Map("tableName" -> finalTableName, "userName" -> tableInfo("userName")), stmt)
      } catch {
        case e : SQLException => e.printStackTrace(); 
        case _ : Throwable => println("Some other error");
      } finally {
        if (!parseSuccess) conn.rollback()
      }
    }
    parseSuccess
  }

  /**
    * Checks to see if the user's selected table name is the name of a table which
    * already exists in the DB. Adds _versN (where N is incremented) until
    * there is no such table with the name
    *
    * @param tableName the desired table name input by user
    * @param conn      the current DB connection
    * @return a unique table name
    */
  private def returnUniqueTableName(tableName: String, conn: Connection): String = {
    var finished = false
    var counter = 1
    var finalName = tableName
    while (!finished) {
      val resultSet = conn.getMetaData.getTables(null, null, finalName, null)
      if (!resultSet.next()) {
        finished = true
      } else {
        finalName = tableName + "_vers" + counter
        counter += 1
      }
    }
    finalName
  }

  /**
    * Creates a brand new table in the DB parsed from a CSV file
    * Assumes file has headers
    * Plucks first line of content out mapped to headers -> contents to figure out
    * datatypes for each column. Then creates a query string creating a table
    * with those column names and their types, then executes that query.
    *
    * @param file      the CSV file we are parsing
    * @param tableName the SQL formatted name of the table to be created
    * @param stmt      the statement from current DB connection
    * @return          true if 1
    */
  @throws(classOf[SQLException])
  private def createNewTable(file: File, tableName: String, stmt: Statement): Unit = {
    val csvReader = CSVReader.open(file)
    val fileContentsWHeaders: List[Map[String, String]] = csvReader.allWithHeaders()
    val firstContentLine: Map[String, String] = fileContentsWHeaders.lift(0).getOrElse(Map())
    val headersToTypes = mapHeadersToSQLDataTypes(firstContentLine)
    val queryBuilder = StringBuilder.newBuilder.append(s"CREATE TABLE $tableName (")
    for ((colName, dataType) <- headersToTypes) queryBuilder.append(s" $colName $dataType, ")
    queryBuilder.append("uploaded_by text, date_added timestamp DEFAULT DATE_TRUNC('second', NOW()) );")
    stmt.executeUpdate(queryBuilder.toString())
  }

  /**
    * Examines the first row of content in headerNames -> content format and
    * runs a matching algorithm to determine the content's SQL data type, returning a map
    * of strings representing: columnName -> datatype.
    * Also runs the column names through a "cleaner" which ensures the headers will be in
    * legal SQL column name format
    *
    * @param firstRow the first row of content from a CSV file mapped with headerName -> content
    * @return alters the value of the original map to be string representation of datatype of the column
    */
  private def mapHeadersToSQLDataTypes(firstRow: Map[String, String]): Map[String, String] = {
    for ((k, v) <- firstRow)
      yield (SQLStringFormatter.returnStringInSQLNameFormat(k), dataTypeFinder(v.toLowerCase))
  }

  private def dataTypeFinder(value: String): String = value.toLowerCase match {
    case value if value.matches("[a-z]+") && !value.matches(".*([0-2]{1}[0-9]{1}:{1}[0-5]{1}[0-9]{1})+.*") => "text" // if contains letters but no 'num:num'
    case value if value.matches("[-]?[0-9]+[.]{1}[0-9]+") => "decimal" // if contains number(s).number(s)
    case value if value.matches("[-]?[0-9]+") => "bigint" // if contains only num, starting with an optional "-"
    case value if value.matches(".*([0-2]{1}[0-9]{1}:{1}[0-5]{1}[0-9]{1})+.*") => "timestamp" // if contains num num :num num
    case value if value.matches("^true$|^false$|^t$|^f$|^y$|^n$|^yes$|^no$") => "boolean"
    case _ => "text"
  }

  /**
    * Assumes file will always have headers
    * Adds data from a CSV file to an existing table in the default database
    * Opens a CSV file, extracts the data as (headers -> content) map
    * Builds a series of queries
    *
    * @param file      the CSV file with data to append - must have a header matching PSQL table's column names
    * @param tableInfo info about PSQL table in the database from the request. Keys are "userName" and "tableName"
    * @param stmt      the statement from the current DB connection
    * @return True if table is larger after operation, false if not
    */
  @throws(classOf[SQLException])
  override def addDataRowsToTable(file: File, tableInfo: Map[String, String], stmt: Statement): Boolean = {
    val csvReader = CSVReader.open(file)
    val data = csvReader.allWithHeaders()
    val queryBuilder = StringBuilder.newBuilder
    val rowTotalBefore = {
      val result = stmt.executeQuery(s"SELECT COUNT(*) AS count FROM ${tableInfo("tableName")};")
      if (result.next()) result.getString("count").toInt else 0
    }
    for (headToContentMap <- data) {
      queryBuilder.append(s"INSERT INTO ${tableInfo("tableName")}(")
      for ((header, content) <- headToContentMap) {
        queryBuilder.append(s"${SQLStringFormatter.returnStringInSQLNameFormat(header)}, ") // creates all column name values
      }
      queryBuilder.append("uploaded_by) VALUES (")
      for ((header, content) <- headToContentMap) {
        dataTypeFinder(content) match { // some datatypes require ' ' around inputs
          case "text" => queryBuilder.append (s"'$content', ")
          case "boolean" => queryBuilder.append (s"'$content', ")
          case "timestamp" => queryBuilder.append (s"'$content', ")
          case _ => queryBuilder.append (s"$content, ")
        }
      }
      queryBuilder.append(s"'${tableInfo("userName")}'); \n") // closes query
    }
    stmt.executeUpdate(queryBuilder.toString())
    var operationAddedData: Boolean = false
    val rowTotalAfter = stmt.executeQuery(s"SELECT COUNT(*) as count FROM ${tableInfo("tableName")}")
    while (rowTotalAfter.next()) {
      if (rowTotalBefore < rowTotalAfter.getString("count").toInt) operationAddedData = true
    }
    operationAddedData
  }

  /**
    * Method which links the creation of a new table to the rest of the database by giving it an
    * entry into the DB table all_data_tables
    * @param rawTableName The name the user entered when creating the table - non-SQL format
    * @param sqlTableName The table name sanitized for SQL naming conventions
    * @param projectTitle The name of the project the table was originally uploaded to.
    * @return true if 1 row is inserted into all_data_tables, false if less or more.
    */
  @throws(classOf[SQLException])
  private def addNewTableToAllDataTables(rawTableName:String, sqlTableName:String, projectTitle:String): Boolean = {
    twDB.withConnection{ conn =>
      val prepStmt = conn.prepareStatement("INSERT INTO all_data_tables VALUES(?, ?, ?)")
      prepStmt.setString(1, rawTableName)
      prepStmt.setString(2, sqlTableName)
      prepStmt.setString(3, projectTitle)
      val rowsUpdated = prepStmt.executeUpdate()
      rowsUpdated == 1
    }
  }


}
