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

  implicit def pagination = Pagination( offset, limit, sortBy )

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

  def totalAmount_=( value : Long ) {
    response.addHeader( "total_amount", value.toString )
  }

}

case class Pagination( offset : Option[Int], limit : Option[Int], sortBy : Map[String, Int] )