package org.jetbrains.plugins.scala
package codeInspection
package shadow

import com.intellij.openapi.project.Project
import com.intellij.codeInspection.{ProblemDescriptor, ProblemsHolder}
import lang.psi.impl.ScalaPsiElementFactory
import lang.psi.api.base.patterns.{ScCaseClause, ScReferencePattern}
import com.intellij.psi.{ResolveResult, PsiNamedElement, PsiElement}
import lang.psi.ScalaPsiUtil
import lang.resolve.processor.ResolveProcessor
import lang.psi.api.base.ScStableCodeReferenceElement
import lang.resolve.{ResolvableStableCodeReferenceElement, StdKinds}

class VariablePatternShadowInspection extends AbstractInspection("VariablePatternShadow", "Suspicious shadowing by a Variable Pattern") {

  def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Any] = {
    case refPat: ScReferencePattern => check(refPat, holder)
  }

  private def check(refPat: ScReferencePattern, holder: ProblemsHolder) {
    val isInCaseClause = ScalaPsiUtil.nameContext(refPat).isInstanceOf[ScCaseClause]
    if (isInCaseClause) {
      val dummyRef: ScStableCodeReferenceElement = ScalaPsiElementFactory.createReferenceFromText(refPat.name, refPat.getContext.getContext, refPat)
      val proc = new ResolveProcessor(StdKinds.valuesRef, dummyRef, refPat.name)
      val results = dummyRef.asInstanceOf[ResolvableStableCodeReferenceElement].doResolve(dummyRef, proc)
      def isAccessible(rr: ResolveResult): Boolean = rr.getElement match {
        case named: PsiNamedElement => proc.isAccessible(named, refPat)
        case _ => false
      }
      if (results.exists(isAccessible)) {
        holder.registerProblem(refPat.nameId, getDisplayName, new ConvertToStableIdentifierPatternFix(refPat), new RenameVariablePatternFix(refPat))
      }
    }
  }
}

class ConvertToStableIdentifierPatternFix(ref: ScReferencePattern)
        extends AbstractFix("Convert to Stable Identifier Pattern `%s`".format(ref.getText), ref) {
  def doApplyFix(project: Project, descriptor: ProblemDescriptor) {
    val stableIdPattern = ScalaPsiElementFactory.createPatternFromText("`%s`".format(ref.getText), ref.getManager)
    ref.replace(stableIdPattern)
  }
}

class RenameVariablePatternFix(ref: ScReferencePattern) extends RenameElementQuickfix(ref, "Rename Variable Pattern")