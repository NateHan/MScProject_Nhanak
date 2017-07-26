package models.database

import play.api.db.{Database, Databases}

object DefaultDataBase {

  val driver:String = "org.postgresql.Driver"
  val url:String = "postgres://twadmin:trackwild@localhost:5432/track_wild_db"

  def getApplicationDataBase:Database = Databases(driver, url)
}
