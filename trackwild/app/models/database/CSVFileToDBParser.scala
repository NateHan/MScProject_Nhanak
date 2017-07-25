package models.database
import java.io.File
import java.sql.{Connection, ResultSet, Statement}
import javax.inject.Inject

import play.api.db.Database

@Singleton
class CSVFileToDBParser @Inject() (twDB:Database) extends FileToDBParser {

  override def parseFileToNewRelation(file: File, tableName:String): Boolean = {
    twDB.withConnection { conn =>
      val stmt = conn.createStatement()
      val finalTableName = returnUniqueTableName(tableName, conn)
      createNewTable(file, finalTableName, stmt)
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
        finished = true;
      } else {
        finalName = tableName + "_vers" + counter
        counter+= 1
      }
    }
    return finalName
  }

  private def createNewTable(file:File, tableName:String, stmt:Statement): Unit = ???

  override def appendFileToExistingRelation(file: File, tableName:String):Boolean = ???

  /**
    *
    * @param file
    * @param tableName
    * @param stmt
    * @return True if table is larger after operation, false if not
    */
  private def addDataRowsToTable(file:File, tableName:String, stmt:Statement): Boolean = ???


}
