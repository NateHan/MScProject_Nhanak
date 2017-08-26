package models.jsonmodels

import play.api.libs.json._

case class ManualRowAddContent(colName:String, value:String)

object ManualRowAddContent {
  implicit val reads: Reads[ManualRowAddContent] = Json.reads[ManualRowAddContent]
}
