package models.database

import play.api.db.Database


object DatabaseUpdate {

  /**
    * Method which executes an SQL INSERT statement into the database.
    * Will automatically add ' ' around all input values to insure they are legal SQL
    *
    * @param tableName     The name of the table on which to perform the insert.
    * @param columnsToVals A map of the table's ( columns -> valuesToinsert).
    * @return Int the number of rows which were affected
    */
  def insertInto(db: Database, tableName: String, columnsToVals: Map[String, String]): Int = {
    val columnBuilder = new StringBuilder
    val valueBuilder = new StringBuilder
    var rowsUpdated: Int = 0
    for ((k, v) <- columnsToVals) {
      columnBuilder.append(k + ", ");
      if (v.startsWith("'") && v.endsWith("'")) {
        valueBuilder.append(v + ", ")
      } else {
        valueBuilder.append("'" + v + "', ")
      }
    }
    columnBuilder.deleteCharAt(columnBuilder.length - 2) //remove erroneous comma
    valueBuilder.deleteCharAt(valueBuilder.length - 2) //remove erroneous comma
    val insertQuery = "INSERT INTO ?(?) VALUES(?);"
    db.withConnection { conn =>
      val prepStmt = conn.prepareStatement(insertQuery)
      prepStmt.setString(1, tableName)
      prepStmt.setString(2, ???) // TODO
      prepStmt.setString(3, ???) // TODO
      val stmt = conn.createStatement()
      rowsUpdated = stmt.executeUpdate(s"INSERT INTO $tableName(${columnBuilder.toString()}) VALUES(${valueBuilder.toString()});")
    }
    rowsUpdated
  }

}
