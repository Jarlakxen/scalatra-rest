package com.github.jarlakxen.scalatra.rest.queryable

import org.scalatra.ScalatraBase
import org.json4s._
import org.scalatra.json.JsonSupport

trait QueryableViewSupport[T] extends JsonSupport[T] {
  self : ScalatraBase =>

  val filterQueryParameterName = "fields"

  def selectedFields = params.get( filterQueryParameterName ) match {
    case Some( fields ) => fields.split( "," ).map( _.trim ).toSeq
    case _ => Seq();
  }

  protected override def transformResponseBody( _body : JValue ) : JValue = {
    val body = super.transformResponseBody( _body );
    val fields = selectedFields

    if ( fields.isEmpty ) return body

    if ( body.isInstanceOf[JArray] ) {
      var elements = body.asInstanceOf[JArray].children
      JArray( for ( element <- elements ) yield { element removeField { field => !( fields contains ( field._1 ) ) } } )
    } else {
      body removeField { field => !( fields contains ( field._1 ) ) }
    }
  }
}

package jackson {
  import com.github.jarlakxen.scalatra.rest.queryable.{ QueryableViewSupport => BaseQueryableViewSupport }
  import org.json4s.JValue

  trait QueryableViewSupport extends BaseQueryableViewSupport[JValue] {}
}

package native {
  import com.github.jarlakxen.scalatra.rest.queryable.{ QueryableViewSupport => BaseQueryableViewSupport }
  import scala.text.Document

  trait QueryableViewSupport extends BaseQueryableViewSupport[Document] {}
}