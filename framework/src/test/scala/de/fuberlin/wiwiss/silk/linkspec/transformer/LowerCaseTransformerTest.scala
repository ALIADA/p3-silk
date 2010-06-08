package de.fuberlin.wiwiss.silk.linkspec.transformer

import org.scalatest.matchers.{ShouldMatchers}
import org.scalatest.FlatSpec
import de.fuberlin.wiwiss.silk.transformer.LowerCaseTransformer

class LowerCaseTransformerTest extends FlatSpec with ShouldMatchers
{
    val transformer = new  LowerCaseTransformer(Map())

    "LowerCaseTransformer" should "return '123'" in
    {
        transformer.evaluate(List("123")) should equal ("123")
    }

    val transformer1 = new  LowerCaseTransformer(Map())

    "LowerCaseTransformer" should "return 'abc'" in
    {
        transformer1.evaluate(List("ABc")) should equal ("abc")
    }
}