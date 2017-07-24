package models.database

import java.io.File


trait FileToDBParser {

  def parseFileToDB(file:File): Unit

  def parseWasSuccesful: Boolean

}
