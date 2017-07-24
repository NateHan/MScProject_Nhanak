package models.formdata

import java.text.SimpleDateFormat
import java.util.Date

case class NewDataTableInfo(tableName:String, uploadingUser:String) {

  val createdDate:String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())

  def getTableNameSQLFormat:String = tableName.replace(" ", "_").toLowerCase()

  def getCreatedDate = createdDate

}
/*
object newDataTableInfo{

  def apply(tableName:String, uploadingUser:String) = {
    val dateCreated =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
    new NewDataTableInfo(tableName, uploadingUser, dateCreated)
  }

}
*/