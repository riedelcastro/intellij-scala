package org.jetbrains.plugins.scala
package lang.psi.controlFlow.impl

import org.jetbrains.plugins.scala.lang.psi.controlFlow.ScControlFlowPolicy
import com.intellij.psi.PsiNamedElement
import org.jetbrains.plugins.scala.lang.psi.api.base.ScReferenceElement

/**
 * Nikolay.Tropin
 * 2014-04-14
 */
object AllVariablesControlFlowPolicy extends ScControlFlowPolicy {
  override def isElementAccepted(named: PsiNamedElement): Boolean = true
}
