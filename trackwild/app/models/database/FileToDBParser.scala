package models.database

import java.io.File
import java.sql.Statement


trait FileToDBParser {

  def parseFileToNewRelation(file: File, tableName:String): Boolean

  def addDataRowsToTable(file:File, tableName:String, stmt:Statement): Boolean = ???


}
