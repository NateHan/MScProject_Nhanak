package models.database

import java.sql.{DatabaseMetaData, ResultSet}

import models.database.DefaultDataBase
import play.api.db.Database

object DatabaseUpdate {

  /**
    * Method which executes an SQL INSERT statement into the database. Checks the column
    * types of the table and will automatically add ' ' around values which need them for legal
    * SQL insert. TEST THIS
    * @param tableName The name of the table on which to perform the insert.
    * @param columnsToVals A map of the table's ( columns -> valuesToinsert).
    * @param expectedRows How many rows are expected to be affected by the insert, used for validation
    * @return Int the number of rows which were affected
    */
  def insertInto(tableName:String, columnsToVals:Map[String,String], expectedRows:Int): Int = {
    val db: Database = DefaultDataBase.getApplicationDataBase
    val columnBuilder = new StringBuilder
    val valueBuilder = new StringBuilder
    db.withConnection{ conn =>
      val dbMetaData = conn.getMetaData
      for( (k,v)  <- columnsToVals) {
        columnBuilder.append(k + ", ");
        val colType: Int = getColumnTypes(dbMetaData, k)
        if colType = TODO // if it matches a texttual type append with ' ' else append without
        valueBuilder.append(v + ", ")
      }
      columnBuilder.deleteCharAt(columnBuilder.length-2) //remove erroneous commas
      valueBuilder.deleteCharAt(valueBuilder.length-2) //remove erroneous commas
     val stmt = conn.createStatement()
      stmt.executeUpdate(s"INSERT INTO $tableName(${columnBuilder.toString()}) VALUES(${valueBuilder.toString()});")
    }
  }

  private def getColumnTypes(metaData:DatabaseMetaData, colName:String): Int = {
    TODO
    //SEE: "Listing Columns in a Table: http://tutorials.jenkov.com/jdbc/databasemetadata.html#listing-columns-in-a-table "
  }

}

//  have input map columns -> vales
//  Check column type
//  check if column type needs ' ' s AND THEN values don't have them
//    -Add ' '
//    -else don't add them