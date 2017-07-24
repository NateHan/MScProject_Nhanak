package models.formdata

import java.text.SimpleDateFormat
import java.util.Date

case class NewDataTableInfo(tableName: String, uploadingUser: String) {

  val createdDate: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())

  /**
    * Replaces all . _ or spaces with an underscore
    * @return a string which is appropriate for an SQL table name
    */
  def getTableNameSQLFormat: String = {
    val charSwap = Map(
      ' ' -> '_',
      '-' -> '_',
      '.' -> '_'
    )
    tableName.map(letters => charSwap.getOrElse(letters, letters)).toLowerCase()
  }

  def getCreatedDate = createdDate

}