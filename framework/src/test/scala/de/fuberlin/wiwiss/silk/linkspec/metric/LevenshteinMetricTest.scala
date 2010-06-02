package de.fuberlin.wiwiss.silk.linkspec.metric

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import de.fuberlin.wiwiss.silk.metric.LevenshteinMetric

class LevenshteinMetricTest extends FlatSpec with ShouldMatchers
{
    val metric = new LevenshteinMetric()

    "LevenshteinMetric" should "return distance 3" in
    {
        metric.evaluateDistance("kitten", "sitting") should equal (3)
        metric.evaluateDistance("sitting", "kitten") should equal (3)
    }
}
