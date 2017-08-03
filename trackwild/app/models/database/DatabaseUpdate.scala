package models.database

import java.sql.{ResultSet, Statement}

import models.database.DefaultDataBase
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
  def insertInto(tableName: String, columnsToVals: Map[String, String]): Int = {
    val db: Database = DefaultDataBase.getApplicationDataBase
    val columnBuilder = new StringBuilder
    val valueBuilder = new StringBuilder
    db.withConnection { conn =>
      val stmt = conn.createStatement()
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
      stmt.executeUpdate(s"INSERT INTO $tableName(${columnBuilder.toString()}) VALUES(${valueBuilder.toString()});")
    }
  }

}