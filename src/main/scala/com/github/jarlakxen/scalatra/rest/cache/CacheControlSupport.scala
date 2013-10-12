package com.github.jarlakxen.scalatra.rest.cache
import javax.servlet.http.{ HttpServletResponse, HttpServletRequest }
import com.github.nscala_time.time.Imports._
import org.joda.time.format.DateTimeFormatter

trait CacheControlSupport {
  implicit def request : HttpServletRequest
  implicit def response : HttpServletResponse

  implicit def CacheControlStrategy2Headers( cacheControlStrategy : CacheControlStrategy ) = cacheControlStrategy.headers

  val RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z"
  val dateTimeFormat = DateTimeFormat.forPattern( RFC1123_PATTERN )

  sealed abstract class CacheControlStrategy {
    def headers : Map[String, String]
  }

  case class NoCache() extends CacheControlStrategy {
    override def headers = Map( "Cache-Control" -> "no-store, no-cache, must-revalidate", "Pragma" -> "no-cache" )
  }

  case class MaxAge( duration : Duration ) extends CacheControlStrategy {
    override def headers = Map( "Cache-Control" -> ( "max-age=" + duration.seconds ) )
  }

  case class Expires( expiration : DateTime ) extends CacheControlStrategy {
    override def headers = Map( "Expires" -> dateTimeFormat.print( expiration ) )
  }

  def cacheControl_=( cacheControl : CacheControlStrategy ) {
    for ( ( key, value ) <- cacheControl.headers ) response.addHeader( key, value )
  }

}