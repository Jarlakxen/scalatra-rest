package com.github.jarlakxen.scalatra.rest.json

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable._
import javax.servlet.http.HttpServletRequest
import org.scalatra.{ ScalatraServlet, ScalatraParams }
import org.scalatra.test.specs2.MutableScalatraSpec
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.auth.ScentrySupport
import org.json4s.{ DefaultFormats, Formats }
import java.util.Date
import org.scalatra.auth.ScentryConfig

@RunWith( classOf[JUnitRunner] )
class JsonViewSupportSpec extends MutableScalatraSpec {

  case class User( id : String, name : String, password : String )

  case class Post( id : String, title : String, content : String, creationDate : String, hidden : Boolean )

  class JsonViewServlet( withUser : Boolean ) extends ScalatraServlet with ScentrySupport[User] with JacksonJsonSupport with jackson.JsonViewSupport[Post, User] {
    protected implicit val jsonFormats : Formats = DefaultFormats

    override val definition : ViewModule[Post, User] => Unit = { definition =>
      import definition._;

      `object` notIf { implicit params => isNotLogged && target.hidden }

      "creationDate" onlyIf { implicit params => isLogged }
    }

    override val targetClass = classOf[Post]

    implicit protected override def user( implicit request : HttpServletRequest ) = fromSession( "1" )
    protected val scentryConfig = ( new ScentryConfig {} ).asInstanceOf[ScentryConfiguration]
    protected def fromSession = {
      case id : String if withUser == true => User( "1", "test", "12345678" )
      case _ => null
    }
    protected def toSession = { case usr : User => usr.id }

    get( "/Default" ){
      contentType = formats( "json" )
      Post( "1", "test", "Hello World!", "1/1/2013", false )
    }

    get( "/DefaultList" ){
      contentType = formats( "json" )
      List( Post( "1", "test", "Hello World!", "1/1/2013", false ) )
    }

    get( "/HiddenObject" ){
      contentType = formats( "json" )
      Post( "1", "test", "Hello World!", "1/1/2013", true )
    }

    get( "/ListWithHiddenObjects" ){
      contentType = formats( "json" )
      List( Post( "1", "test", "Hello World!", "1/1/2013", true ) )
    }

  }

  addServlet( new JsonViewServlet( true ), "/Logged/*" )
  addServlet( new JsonViewServlet( false ), "/Unlogged/*" )

  "JsonViewServlet" should {
    "return the full object when is logged" in {
      get( "/Logged/Default" ) {
        status must_== 200
        body must_== "{\"id\":\"1\",\"title\":\"test\",\"content\":\"Hello World!\",\"creationDate\":\"1/1/2013\",\"hidden\":false}"
      }

      get( "/Logged/DefaultList" ) {
        status must_== 200
        body must_== "[{\"id\":\"1\",\"title\":\"test\",\"content\":\"Hello World!\",\"creationDate\":\"1/1/2013\",\"hidden\":false}]"
      }
      get( "/Logged/HiddenObject" ) {
        status must_== 200
        body must_== "{\"id\":\"1\",\"title\":\"test\",\"content\":\"Hello World!\",\"creationDate\":\"1/1/2013\",\"hidden\":true}"
      }

      get( "/Logged/ListWithHiddenObjects" ) {
        status must_== 200
        body must_== "[{\"id\":\"1\",\"title\":\"test\",\"content\":\"Hello World!\",\"creationDate\":\"1/1/2013\",\"hidden\":true}]"
      }
    }

    "return a partial object when is not logged and the object is not hidden" in {
      get( "/Unlogged/Default" ) {
        status must_== 200
        body must_== "{\"id\":\"1\",\"title\":\"test\",\"content\":\"Hello World!\",\"hidden\":false}"
      }

      get( "/Unlogged/DefaultList" ) {
        status must_== 200
        body must_== "[{\"id\":\"1\",\"title\":\"test\",\"content\":\"Hello World!\",\"hidden\":false}]"
      }
    }

    "return non object when is not logged and the object is hidden" in {
      get( "/Unlogged/HiddenObject" ) {
        status must_== 200
        body must_== ""
      }
      get( "/Unlogged/ListWithHiddenObjects" ) {
        status must_== 200
        body must_== "[]"
      }
    }

  }

}