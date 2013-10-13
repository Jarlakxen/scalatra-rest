package com.github.jarlakxen.scalatra.rest.queryable

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable._
import javax.servlet.http.HttpServletRequest
import org.scalatra.{ ScalatraServlet, ScalatraParams }
import org.scalatra.test.specs2.MutableScalatraSpec
import org.scalatra.json.JacksonJsonSupport
import org.json4s.{ DefaultFormats, Formats }

@RunWith( classOf[JUnitRunner] )
class QueryableViewSupportSpec extends MutableScalatraSpec {

  case class User( id : String, name : String, password : String )

  class QueryableViewServlet extends ScalatraServlet with JacksonJsonSupport with jackson.QueryableViewSupport {
    protected implicit val jsonFormats : Formats = DefaultFormats
    get( "/" ){
      contentType = formats( "json" )
      User( "1", "test", "1233456" )
    }
  }

  addServlet( new QueryableViewServlet, "/*" )

  "GET / on QueryableViewServlet" should {
    "return status 200 and the full object" in {
      get( "/" ) {
        status must_== 200
        body must_== "{\"id\":\"1\",\"name\":\"test\",\"password\":\"1233456\"}"
      }
    }
    "return status 200 and the partial object" in {
      get( "/", Map( "fields" -> "id, name" ) ) {
        status must_== 200
        body must_== "{\"id\":\"1\",\"name\":\"test\"}"
      }
    }
  }

}