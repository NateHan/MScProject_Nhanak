package models.database

import java.sql.{PreparedStatement, Timestamp}

import play.api.db.Database

import scala.collection.mutable.ListBuffer


object DatabaseUpdate {

  /**
    * Method which executes an SQL INSERT statement into the database by row.
    * Will automatically add ' ' around all input values to insure they are legal SQL
    *
    * @param tableName     The name of the table on which to perform the insert.
    * @param columnsToVals A map of the table's ( columns -> valuesToinsert).
    * @return Int the number of rows which were affected
    */
  def insertRowInto(db: Database, tableName: String, columnsToVals: Map[String, String]): Int = {
    val (columns, valueList) = prepareColumnsAndRows(columnsToVals)
    var insertQuery = s"INSERT INTO $tableName($columns) VALUES(${createQMarks(columnsToVals.size)})"
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement(insertQuery)
      for (i <- 1 to valueList.size) {
        parseTypeAndSetPrepStmt(valueList(i-1), prepStmt, i)
      }
      prepStmt.executeUpdate() // returns an Int with # of rows updated
    }
  }

  /**
    * Takes the map collection and prepares the values as strings for use in a prepared statement
    *
    * @param colsToVals a row mapped out as columnNames -> column values
    * @return a flattened string of the columns' names and a list of values to put into an SQL query
    */
  private def prepareColumnsAndRows(colsToVals: Map[String, String]): (String, List[String]) = {
    val columnBuilder = new StringBuilder
    val numOfEntries = colsToVals.size
    val valueList = new ListBuffer[String]
    var counter = 1
    for ((k, v) <- colsToVals) {
      columnBuilder.append(k + ", ")
      valueList += v
    }
    columnBuilder.deleteCharAt(columnBuilder.length - 2) //remove erroneous comma
    (columnBuilder.toString(), valueList.toList)
  }

  /**
    * Takes the number of inputs for the desired query string, and inserts the appropriate amount
    * of prepared statement parameters - in this case the form of "?"'s
    *
    * @param numOfValues the number of input values for the update statement
    * @return a list of ? marks, one for each input.
    */
  private def createQMarks(numOfValues: Int): String = {
    val builder = new StringBuilder
    for (i <- 1 to numOfValues) {
      if (i < numOfValues) builder.append("?,") else builder.append("?")
    }
    builder.toString()
  }

  /**
    * Method which takes a value in the form of a string to be used in a DB INSERT statement.
    * Since all values come in initially as String, they will need to be parsed to their proper form
    * for insert into the DB.
    *
    * @param value    the value item to be INSERT into the DB row.
    * @param pStmt    PreparedStatement to be executed for the insert
    * @param position The index position of the @value within the @pStmt's query string.
    */
  private def parseTypeAndSetPrepStmt(value: String, pStmt: PreparedStatement, position: Int): Unit = {
    value match {
      case value if value.toLowerCase.matches("[a-z]+") && !value.matches(".*([0-2]{1}[0-9]{1}:{1}[0-5]{1}[0-9]{1})+.*") => pStmt.setString(position, value) // if contains letters but no 'num:num'
      case value if value.matches("[-]?[0-9]+[.]{1}[0-9]+") => pStmt.setDouble(position, value.toDouble)  // if contains number(s).number(s)
      case value if value.matches("[-]?[0-9]+") => pStmt.setInt(position, value.toInt) // if contains only num, starting with an optional "-"
      case value if value.matches(".*([0-2]{1}[0-9]{1}:{1}[0-5]{1}[0-9]{1})+.*") => pStmt.setTimestamp(position, Timestamp.valueOf(value)) // if contains num num :num num
      case value if value.toLowerCase.matches("^true$|^false$|^t$|^f$|^y$|^n$|^yes$|^no$") => pStmt.setBoolean(position, value.toBoolean)
      case _ => pStmt.setString(position, value)
    }
  }

  /**
    * Method which removes a row from a table
    * @param whereStatement the qualifying piece of the SQL query to be executed
    * @param tableName the name of the table to be updated
    * @param db the database location of the table
    * @return an Int containing the number of rows affected.
    */
  def removeFromTable(whereStatement:String, tableName:String, db:Database): Boolean = {
    db.withConnection { conn =>
      val stmt = conn.createStatement()
      val numRowsAffected:Int = stmt.executeUpdate(s"DELETE FROM $tableName WHERE $whereStatement;")
      numRowsAffected == 1
    }
  }

}
