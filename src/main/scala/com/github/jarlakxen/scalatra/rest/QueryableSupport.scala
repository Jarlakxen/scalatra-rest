package com.github.jarlakxen.scalatra.rest

import org.scalatra.ScalatraBase
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import java.lang.reflect.ParameterizedType
import java.util.concurrent.ConcurrentHashMap

trait QueryableSupport {
  self : ScalatraBase =>

  case class CaseClassField( name : String, `type` : RuntimeClass )

  private val mirror = runtimeMirror( getClass.getClassLoader )
  private val cache = new ConcurrentHashMap[TypeTag[_], Seq[CaseClassField]]

  def paramsOf[T]( ignoreNotQueryable : Boolean )( implicit ttag : TypeTag[T] ) : Map[String, Any] = paramsOf[T]( ttag, ignoreNotQueryable )

  def paramsOf[T]( implicit ttag : TypeTag[T], ignoreNotQueryable : Boolean = false ) : Map[String, Any] = {

    if ( !cache.contains( ttag ) ) {
      cache.put( ttag, extract( ignoreNotQueryable, ttag ) )
    }

    val fields = cache.get( ttag )

    params.filter( param => fields.exists( _.name == param._1 ) )
      .map( param => fields.find( _.name == param._1 ).get.`type` match {
        case tp if tp == classOf[Boolean] => ( param._1, param._2.toBoolean )
        case tp if tp == classOf[Int] => ( param._1, param._2.toInt )
        case tp if tp == classOf[Long] => ( param._1, param._2.toLong )
        case tp if tp == classOf[Float] => ( param._1, param._2.toFloat )
        case tp if tp == classOf[Double] => ( param._1, param._2.toDouble )
        case _ => param
      } )
  }

  private def extract[T]( ignoreNotQueryable : Boolean, ttag : TypeTag[T] ) : Seq[CaseClassField] = {
    val cto = ttag.tpe.member( nme.CONSTRUCTOR ).asMethod
    cto.paramss.head.collect{
      case p : TermSymbol if ignoreNotQueryable || !p.annotations.exists( _.tpe =:= typeOf[NotQueryable] ) => p
    }.map( param => CaseClassField( param.name.toString, mirror.runtimeClass( param.typeSignature.typeSymbol.asClass ) ) ).toSeq
  }

}