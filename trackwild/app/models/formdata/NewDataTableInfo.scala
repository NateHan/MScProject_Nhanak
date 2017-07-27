package models.formdata

import java.text.SimpleDateFormat
import java.util.Date

import models.database.SQLStringFormatter

case class NewDataTableInfo(tableName: String, uploadingUser: String) {

  val createdDate: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())

  /**
    * Method which searches for characters which would be an illegal
    * character to have for a table or column name in SQL and replaces
    * them with a legal SQL table or column name format
    *
    * @param input The string which we are converting
    * @return A string stripped of its illegal SQL characters
    */
  def getTableNameSQLFormat: String = SQLStringFormatter.returnStringInSQLNameFormat(tableName)

  def getCreatedDate = createdDate

}