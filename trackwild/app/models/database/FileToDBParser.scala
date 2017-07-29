package models.database

import java.io.File
import java.sql.Statement


trait FileToDBParser {

  def parseFileToNewRelation(file: File, tableInfo:Map[String,String]): Boolean

  def addDataRowsToTable(file: File, tableInfo: Map[String, String], stmt: Statement): Boolean


}
