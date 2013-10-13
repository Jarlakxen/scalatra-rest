package com.github.jarlakxen.scalatra.rest

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable._
import javax.servlet.http.HttpServletRequest
import org.scalatra.{ ScalatraServlet, ScalatraParams }

@RunWith( classOf[JUnitRunner] )
class QueryableSupportSpec extends Specification {

  case class User( name : String, email : String, @NotQueryable password : String, enabled : Boolean )

  "QueryableSupport trait" should {

    val base = new ScalatraServlet with QueryableSupport {
      override def params( implicit request : HttpServletRequest ) = new ScalatraParams( Map( "enabled" -> Seq( "false" ), "password" -> Seq( "12345" ), "fake" -> Seq( "fake" ) ) )
    }

    "get field enabled" in {

      base.paramsOf[User] must have size ( 1 )

      base.paramsOf[User] must havePair( "enabled" -> false )
    }

    "get field password ignoring not queryable" in {

      base.paramsOf[User]( ignoreNotQueryable = true ) must have size ( 2 )
    }
  }
}