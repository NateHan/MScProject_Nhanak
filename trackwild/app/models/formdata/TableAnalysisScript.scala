package models.formdata

sealed trait TableAnalysisScript
case class TableSQLScript(viewName:String, query:String) extends TableAnalysisScript
case class TableRScript(tableName:String, rScript:String) extends TableAnalysisScript
