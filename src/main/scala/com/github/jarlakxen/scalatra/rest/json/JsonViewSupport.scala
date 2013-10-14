package com.github.jarlakxen.scalatra.rest.json

import scala.util.DynamicVariable
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentrySupport
import org.scalatra.json.JsonSupport

trait JsonViewSupport[Target <: AnyRef, UserType <: AnyRef, JsonType <: AnyRef] extends ScalatraBase with JsonSupport[JsonType] {
  self : ScentrySupport[UserType] =>

  val definition : ViewModule[Target, UserType] => Unit

  lazy val rules = {
    val module = new Object with ViewModule[Target, UserType]
    definition( module )
    module.rules
  }

  private val resultCache = new DynamicVariable[Any]( null )

  protected override def renderResponse( actionResult : Any ) {

    val objectRules = rules._1;
    var result = actionResult

    if ( result.isInstanceOf[Traversable[Target]] ) {

      // If the result is an array remove the elements that doesn't match
      result = result.asInstanceOf[Traversable[Target]].filter( v => !objectRules.exists( rule => rule.condition( RuleParameters( v, user ) ) ) )

    } else {

      // if the result is a single object , check the rules
      if ( objectRules.exists( _.condition( RuleParameters( result.asInstanceOf[Target], user ) ) ) ) {
        result = null
      }
    }

    resultCache.value = result

    super.renderResponse( result )

  }

  protected override def transformResponseBody( _body : JValue ) : JValue = {
    val body = super.transformResponseBody( _body );

    val result = resultCache.value
    if ( result == null ) {
      return body
    }

    val fieldsRules = rules._2;

    if ( body.isInstanceOf[JArray] ) {
      var elements = body.asInstanceOf[JArray].children
      val values = result.asInstanceOf[Traversable[Target]].toList

      elements = ( for ( ( element, value ) <- elements zip values ) yield {
        val fieldsToRemove = fieldsRules.filter( _.condition( RuleParameters( value, user ) ) ).map( _.fieldName )
        element removeField { field => fieldsToRemove contains ( field._1 ) }
      } ).toList

      JArray( elements )
    } else {
      val fieldsToRemove = fieldsRules.filter( _.condition( RuleParameters( result.asInstanceOf[Target], user ) ) ).map( _.fieldName )

      body removeField { field => fieldsToRemove contains ( field._1 ) }
    }
  }
}

package jackson {
  import com.github.jarlakxen.scalatra.rest.json.{ JsonViewSupport => BaseJsonViewSupport }
  import org.json4s.JValue

  trait JsonViewSupport[Target <: AnyRef, UserType <: AnyRef] extends BaseJsonViewSupport[Target, UserType, JValue] {
    self : ScentrySupport[UserType] =>
  }
}

package native {
  import com.github.jarlakxen.scalatra.rest.json.{ JsonViewSupport => BaseJsonViewSupport }
  import scala.text.Document

  trait JsonViewSupport[Target <: AnyRef, UserType <: AnyRef] extends BaseJsonViewSupport[Target, UserType, Document] {
    self : ScentrySupport[UserType] =>
  }
}

