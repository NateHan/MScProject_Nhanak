package models.database

import play.api.db.Database

import scala.util.Random

object QueryExecutor {

  /**
    * Method which generates a custom-generated view returning the entirety of the requested table.
    * @param tableName the name of the actual table stored in the DB
    * @param db the DB where the table is located
    * @return the newly-generated view
    */
  def generateViewFor(tableName:String, db:Database): String ={
    val viewName = tableName + Random.alphanumeric.take(5).mkString

    viewName
  }

  /**
    * Method which removes the view by name
    * @param viewName the name of the view to be removed from the DB
    * @param db the DB in which the view is located.
    * @return True if the view was successfully removed, false if not.
    */
  def destroyView(viewName:String, db:Database): Boolean = {
    false
  }


  //TODO make a method which performs a query on a view

}
