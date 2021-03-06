package org.jetbrains.plugins.scala
package codeInspection.methodSignature

import org.jetbrains.plugins.scala.codeInspection.{InspectionBundle, ScalaLightInspectionFixtureTestAdapter}
import com.intellij.codeInspection.LocalInspectionTool

/**
 * Nikolay.Tropin
 * 6/25/13
 */
class DeclarationHasNoExplicitTypeInspectionTest extends ScalaLightInspectionFixtureTestAdapter {
  protected def classOfInspection: Class[_ <: LocalInspectionTool] = classOf[DeclarationHasNoExplicitTypeInspection]
  protected def annotation: String = InspectionBundle.message("declaration.has.no.explicit.type.name")
  private val hint = InspectionBundle.message("add.unit.type.to.declaration")

  def test1(): Unit = {
    val selected = s"def ${START}foo$END()"
    check(selected)
    val text = "def foo()"
    val result = "def foo(): Unit"
    testFix(text, result, hint)
  }

  def test2(): Unit = {
    val selected = s"""def haha()
                     |def ${START}hoho$END()
                     |def hihi()"""
    check(selected)
    val text = s"""def haha()
                 |def ho${CARET_MARKER}ho()
                 |def hihi()"""
    val result = """def haha()
                   |def hoho(): Unit
                   |def hihi()"""
    testFix(text, result, hint)
  }

  def test3(): Unit = {
    val selected = s"def ${START}foo$END(x: Int)"
    check(selected)
    val text = "def foo(x: Int)"
    val result = "def foo(x: Int): Unit"
    testFix(text, result, hint)
  }

  def test4(): Unit = {
    val selected = s"def ${START}foo$END"
    check(selected)
    val text = "def foo"
    val result = "def foo: Unit"
    testFix(text, result, hint)
  }
}
