package org.jetbrains.plugins.scala
package lang
package psi
package api
package base
package types

import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement

/** 
* @author Alexander Podkhalyuzin
* Date: 13.03.2008
*/

trait ScExistentialTypeElement extends ScTypeElement {
  def quantified = findChildByClassScala(classOf[ScTypeElement])
  def clause = findChildByClassScala(classOf[ScExistentialClause])
}