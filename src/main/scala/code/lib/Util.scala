package code.lib

import java.util.Date
import java.text.SimpleDateFormat
import java.text.ParsePosition

object Util {

  def convertUtilDateToString(date: Date, format: String) = {
    try{
    val formatedDate = new SimpleDateFormat(format).format(date)
    formatedDate
    }catch{
      case ex : Throwable =>  ""
    }
  }

}