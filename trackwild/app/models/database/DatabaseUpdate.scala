package models.database

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
    val(columns, valueList) = prepareColumnsAndRows(columnsToVals)
    var insertQuery = s"INSERT INTO $tableName($columns) VALUES(${createQMarks(columnsToVals.size)})"
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement(insertQuery)
      for (i <- 1 to valueList.size) {
        prepStmt.setString(i, valueList(i-1))
      }
      prepStmt.executeUpdate() // returns an Int with # of rows updated
    }
  }

  /**
    * Takes the map collection and prepares the values as strings for use in a prepared statement
    * @param colsToVals a row mapped out as columnNames -> column values
    * @return a flattened string the columns and a list of values to put into an SQL query
    */
  private def prepareColumnsAndRows(colsToVals:Map[String,String]): (String, List[String]) = {
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
    * @param numOfValues the number of input values for the update statement
    * @return a list of ? marks, one for each input.
    */
  private def createQMarks(numOfValues:Int):String = {
    val builder = new StringBuilder
    for (i <- 1 to numOfValues) {
      if (i < numOfValues) builder.append("?,") else builder.append("?")
    }
    builder.toString()
  }

}
