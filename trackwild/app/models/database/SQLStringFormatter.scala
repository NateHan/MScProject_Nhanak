package models.database

object SQLStringFormatter {

  /**
    * Method which searches for characters which would be an illegal
    * character to have for a table or column name in SQL
    *
    * @param input The string which we are converting
    * @return A string stripped of its illegal SQL characters
    */
  def returnStringInSQLNameFormat(input: String): String = {
    val charSwap = Map(
      ' ' -> '_',
      '-' -> '_',
      '.' -> '_',
      ',' -> '_',
      '!' -> '1',
      '?' -> '2',
      ''' -> '_'
    )
    input.map(letters => charSwap.getOrElse(letters, letters)).toLowerCase()
  }

}
