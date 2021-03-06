package org.jetbrains.plugins.scala
package codeInspection.collections

import org.jetbrains.plugins.scala.codeInspection.InspectionBundle

/**
 * Nikolay.Tropin
 * 5/30/13
 */
class FindIsDefinedTest extends OperationsOnCollectionInspectionTest {
  val hint = InspectionBundle.message("find.isDefined.hint")
  def test_1() {
    val selected = s"""val valueIsGoodEnough: (Any) => Boolean = _ => true
                 |Nil.${START}find(valueIsGoodEnough).isDefined$END""".stripMargin
    check(selected)
    val text = """val valueIsGoodEnough: (Any) => Boolean = _ => true
                 |Nil.find(valueIsGoodEnough).isDefined""".stripMargin
    val result = """val valueIsGoodEnough: (Any) => Boolean = _ => true
                   |Nil.exists(valueIsGoodEnough)""".stripMargin
    testFix(text, result, hint)
  }

  def test_2() {
    val selected = s"(Nil ${START}find (_ => true)) isDefined$END"
    check(selected)
    val text = "(Nil find (_ => true)) isDefined"
    val result = "Nil exists (_ => true)"
    testFix(text, result, hint)
  }

  override val inspectionClass = classOf[FindIsDefinedInspection]
}
