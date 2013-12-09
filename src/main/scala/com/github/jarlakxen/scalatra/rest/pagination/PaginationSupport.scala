package com.github.jarlakxen.scalatra.rest.pagination

import org.scalatra.ScalatraBase
import scala.concurrent._

trait PaginationSupport[T] {
  self : ScalatraBase =>

  implicit def offset : Option[Int] = params.get( "page" ) match {
    case Some( value ) => Some( value.toInt * limit.get )
    case _ => None
  }

  implicit def limit : Option[Int] = params.get( "pageSize" ) match {
    case Some( value ) => Some( value.toInt )
    case _ => None
  }

  implicit def sortBy : Map[String, Int] = params.get( "sortBy" ) match {
    case Some( value ) => ( for ( p <- value.split( "," ) ) yield {
      if ( p.startsWith( "-" ) ) ( p.substring( 1 ), -1 )
      else ( p, 1 )
    } ).toMap
    case _ => Map()
  }

  def paginate( resultFunction : => Traversable[T], totalAmountFunction : => Long )( implicit ctx : ExecutionContext ) : Future[Traversable[T]] = {
    val _response = response
    val futureResult = Future{ resultFunction }
    val futureTotalAmount = Future{
      val value = totalAmountFunction;
      _response.addHeader( "total_amount", value.toString )
      value
    }

    for {
      result <- futureResult;
      _ <- futureTotalAmount
    } yield result
  }

  def totalAmount_=( value : Int ) {
    response.addHeader( "total_amount", value.toString )
  }

}