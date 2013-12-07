package com.github.jarlakxen.scalatra.rest.json

import scala.util.DynamicVariable
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentrySupport
import org.scalatra.json.JsonSupport
import org.scalatra.ActionResult
import scala.reflect.ClassTag

trait JsonViewSupport[Target <: AnyRef, UserType <: AnyRef, JsonType <: AnyRef] extends ScalatraBase with JsonSupport[JsonType] {
  self : ScentrySupport[UserType] =>

  val definition : ViewModule[Target, UserType] => Unit

  lazy val module = {
    val module = new Object with ViewModule[Target, UserType]
    definition( module )
    module
  }

  lazy val rules = module.rules

  private val resultCache = new DynamicVariable[Any]( null )

  protected override def renderResponseBody( response : Any ) {

    val objectRules = rules._1;

    var result = response match {
      case actionResult : ActionResult => actionResult.body
      case value => value
    }

    if ( module.targetTraversableClass.isInstance( result ) ) {

      // If the result is an array remove the elements that doesn't match
      result = result.asInstanceOf[Traversable[Target]].filter( v => !objectRules.exists( rule => rule.condition( RuleParameters( v, user ) ) ) )

    } else if ( module.targetClass.isInstance( result ) ) {

      // if the result is a single object , check the rules
      if ( objectRules.exists( _.condition( RuleParameters( result.asInstanceOf[Target], user ) ) ) ) {
        result = null
      }
    }

    resultCache.value = result

    super.renderResponseBody( result )

  }

  protected override def transformResponseBody( _body : JValue ) : JValue = {
    val body = super.transformResponseBody( _body );

    val result = resultCache.value
    if ( result == null || !( module.targetClass.isInstance( result ) || module.targetTraversableClass.isInstance( result ) ) ) {
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

