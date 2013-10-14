package com.github.jarlakxen.scalatra.rest.json

import scala.collection._
import scala.collection.mutable.ListBuffer

trait ViewModule[Target <: AnyRef, UserType <: AnyRef] {

  import mutable.ListBuffer

  @volatile protected[this] var _objectRules = ListBuffer.empty[ViewRule[Target, UserType]]
  @volatile protected[this] var _fieldsRules = ListBuffer.empty[ViewRule[Target, UserType]]

  implicit def String2Rule( fieldName : String ) = ViewRuleBuilder( _fieldsRules, fieldName )

  def objectRules = _objectRules.toList

  def fieldsRules = _fieldsRules.toList

  def rules = ( objectRules, fieldsRules )

  def `object` = ViewRuleBuilder( _objectRules )

  def target( implicit params : RuleParameters[Target, UserType] ) : Target = params.targetObject

  def currentUser( implicit params : RuleParameters[Target, UserType] ) : UserType = params.currentUser

  def isLogged( implicit params : RuleParameters[Target, UserType] ) = params.currentUser != null

  def isNotLogged( implicit params : RuleParameters[Target, UserType] ) = !isLogged

}

case class ViewRuleBuilder[Target <: AnyRef, UserType <: AnyRef]( rules : ListBuffer[ViewRule[Target, UserType]], fieldName : String = null ) {

  def notIf( condition : RuleParameters[Target, UserType] => Boolean ) {
    rules += ViewRule( condition, fieldName )
  }

  def onlyIf( condition : RuleParameters[Target, UserType] => Boolean ) {
    rules += ViewRule( params => !condition( params ), fieldName )
  }

}

case class RuleParameters[Target <: AnyRef, UserType <: AnyRef]( targetObject : Target, currentUser : UserType )

case class ViewRule[Target <: AnyRef, UserType <: AnyRef]( condition : RuleParameters[Target, UserType] => Boolean, fieldName : String = null )
