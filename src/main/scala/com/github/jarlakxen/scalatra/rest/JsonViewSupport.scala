package com.madhouse.web.rest.tools

import scala.util.DynamicVariable
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentrySupport
import org.scalatra.json.JsonSupport
import com.github.jarlakxen.scalatra.rest.{ MutableSecurityModule, ViewModule, RuleParameters }

trait ViewFilter[Target <: AnyRef, UserType <: AnyRef] {
  self : ScalatraBase with ScentrySupport[UserType] with JsonSupport[Target] =>

  val definition : MutableSecurityModule[Target, UserType] => Unit

  lazy val rules = ViewModule.rulesOf( definition )

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

    renderResult( result )

  }

  private def renderResult( actionResult : Any ) {
    if ( contentType == null )
      contentTypeInferrer.lift( actionResult ) foreach {
        contentType = _
      }

    renderResponseBody( actionResult )
  }

  protected override def transformResponseBody( _body : JValue ) : JValue = {

    val body = self.transformResponseBody( _body );

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