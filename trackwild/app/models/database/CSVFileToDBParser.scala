package models.database
import java.io.File

import scala.io.{BufferedSource, Source}
import java.sql.{Connection, ResultSet, Statement}
import javax.inject.Inject
import javax.inject.Singleton

import com.github.tototoshi.csv.CSVReader
import play.api.db.Database

@Singleton
class CSVFileToDBParser @Inject() (twDB:Database) extends FileToDBParser {


  override def parseFileToNewRelation(file: File, tableName:String): Boolean = {
    twDB.withConnection { conn =>
      val stmt = conn.createStatement()
      val finalTableName = returnUniqueTableName(tableName, conn)
      createNewTable(file, finalTableName, stmt)
      addDataRowsToTable(file, tableName, stmt)
    }
  }

  /**
    * Checks to see if the user's selected table name is the name of a table which
    * already exists in the DB. Adds _versN (where N is incremented) until
    * there is no such table with the name
    * @param tableName the desired table name input by user
    * @param conn the current DB connection
    * @return a unique table name
    */
  private def returnUniqueTableName(tableName:String, conn:Connection): String = {
    var finished = false
    var counter = 1
    var finalName = tableName
    while(!finished) {
      val resultSet = conn.getMetaData.getTables(null, null, finalName, null)
      if (!resultSet.next()) {
        finished = true
      } else {
        finalName = tableName + "_vers" + counter
        counter+= 1
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
    * @param file the CSV file we are parsing
    * @param tableName the SQL formatted name of the table to be created
    * @param stmt the statement from current DB connection
    */
  private def createNewTable(file:File, tableName:String, stmt:Statement): Unit = {
    val csvReader = CSVReader.open(file)
    val fileContentsWHeaders : List[Map[String, String]] = csvReader.allWithHeaders()
    val firstContentLine : Map[String, String] = fileContentsWHeaders.lift(0).getOrElse(Map())
    val headersToTypes = mapHeadersToSQLDataTypes(firstContentLine)
    val queryBuilder = StringBuilder.newBuilder.append(s"CREATE TABLE $tableName (")
    for ((colName,dataType) <- headersToTypes ) queryBuilder.append(s" $colName $dataType, ")
    queryBuilder.deleteCharAt(queryBuilder.length()-2) // removes erroneous last comma
    queryBuilder.append(");")
    stmt.executeUpdate(queryBuilder.toString())
  }

  /**
    * Examines the first row of content in headerNames -> content format and
    * runs a matching algorithm to determine the content's SQL data type, returning a map
    * of strings representing: columnName -> datatype.
    * Also runs the column names through a "cleaner" which ensures the headers will be in
    * legal SQL column name format
    * @param firstRow the first row of content from a CSV file mapped with headerName -> content
    * @return alters the value of the original map to be string representation of datatype of the column
    */
  private def mapHeadersToSQLDataTypes(firstRow: Map[String, String]): Map[String, String] = {

    def typeFinder(value: String): String = value match {
      case value if value.matches("[a-z]+") && !value.matches(".*([0-2]{1}[0-9]{1}:{1}[0-5]{1}[0-9]{1})+.*") => "text"  // if contains letters but no 'num:num'
      case value if value.matches("[-]?[0-9]+[.]{1}[0-9]+") => "decimal"   // if contains number(s).number(s)
      case value if value.matches("[-]?[0-9]+") => "bigint"// if contains only num, starting with an optional "-"
      case value if value.matches(".*([0-2]{1}[0-9]{1}:{1}[0-5]{1}[0-9]{1})+.*") => "timestamp"// if contains num num :num num
      case _ => "text"
    }

    for( (k,v) <- firstRow )
      yield (SQLStringFormatter.returnStringInSQLNameFormat(k),typeFinder(v.toLowerCase))

  }

  /**
    * Assumes file will always have headers
    * Adds data from a CSV file to an existing table in the default database
    * Opens a CSV file, extracts the data as (headers -> content) map
    * Builds a series of queries
    * @param file the CSV file with data to append - must have a header matching PSQL table's column names
    * @param tableName the name of the PSQL table in the database
    * @param stmt the statement from the current DB connection
    * @return True if table is larger after operation, false if not
    */
  override def addDataRowsToTable(file:File, tableName:String, stmt:Statement): Boolean = {
    val csvReader = CSVReader.open(file)
    val data = csvReader.allWithHeaders()
    val queryBuilder = StringBuilder.newBuilder
    val rowTotalBefore = stmt.executeQuery(s"SELECT COUNT(*) FROM $tableName;")
    for (headToContentMap <- data ) {
      queryBuilder.append(s"INSERT INTO $tableName(")
      for( (header,content) <- headToContentMap) {
        queryBuilder.append(s"${SQLStringFormatter.returnStringInSQLNameFormat(header)}, ") // creates all column name values
      }
      queryBuilder.deleteCharAt(queryBuilder.length()-2) // removes erroneous last comma
      queryBuilder.append(") VALUES (")
      for( (header,content) <- headToContentMap) {
        queryBuilder.append(s"$content, ") // creates all content values to insert
      }
      queryBuilder.deleteCharAt(queryBuilder.length()-2) // removes erroneous last comma
      queryBuilder.append("); \n") // closes query
    }
    stmt.executeUpdate(queryBuilder.toString())
    val rowTotalAfter = stmt.executeQuery(s"SELECT COUNT(*) FROM $tableName")
    var operationAddedData: Boolean = false
    while(rowTotalBefore.next() && rowTotalAfter.next()) {
      if (rowTotalBefore.getString("count").toInt < rowTotalAfter.getString("count").toInt) operationAddedData = true
    }
    operationAddedData
  }





}
