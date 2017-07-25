package models.database

import java.io.File


trait FileToDBParser {

  def parseFileToNewRelation(file: File, tableName:String): Boolean

  def appendFileToExistingRelation(file: File, tableName:String): Boolean


}
