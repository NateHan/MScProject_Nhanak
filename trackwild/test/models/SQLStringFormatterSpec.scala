package models

import models.database.SQLStringFormatter
import org.scalatestplus.play.PlaySpec

class SQLStringFormatterSpec extends PlaySpec {

  "SQLStringFormatter#returnStringInSQLNameFormat " should {

    "convert strings which would be illegal format for SQL table and col names " in {

      val badString1 = "Bad string!"
      val badString2 = "Bad string"
      val badString3 = "Bad-string"
      val badString4 = "Bad string?"
      val badString5 = "Bad'String"
      val badString6 = "Bad.String"
      val badString7 = "Bad,String"

      SQLStringFormatter.returnStringInSQLNameFormat(badString1) mustBe "bad_string1"
      SQLStringFormatter.returnStringInSQLNameFormat(badString2) mustBe "bad_string"
      SQLStringFormatter.returnStringInSQLNameFormat(badString3) mustBe "bad_string"
      SQLStringFormatter.returnStringInSQLNameFormat(badString4) mustBe "bad_string2"
      SQLStringFormatter.returnStringInSQLNameFormat(badString5) mustBe "bad_string"
      SQLStringFormatter.returnStringInSQLNameFormat(badString6) mustBe "bad_string"
      SQLStringFormatter.returnStringInSQLNameFormat(badString7) mustBe "bad_string"
    }
  }

}
