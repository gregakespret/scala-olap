package com.olap

import com.olap.Constants._

case class Query(
  metrics: Set[Metric],
  dimensions: Set[Dimension],
  filters: Set[Filter]
) {
  override def toString = s"(" +
    s"\n  metrics    = $metrics" +
    s"\n  dimensions = $dimensions" +
    s"\n  filters    = $filters" +
    s"\n)\n"
}