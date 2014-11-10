package com.olap

object Constants {
  trait Metric { def name: String }

  case class MaterializedMetric(name: String) extends Metric {
    override def toString = name
  }

  case class ComputedMetric(
    name: String,
    metrics: Set[MaterializedMetric],
    dimensionDependencies: Set[Dimension] = Set()  // this could be moved to individual MaterializedMetric, but whatever
  ) extends Metric

  implicit def fromStringToMaterializedMetric(name: String): MaterializedMetric = Constants.materializedMetrics(name)

  type Dimension = String
  type Filter = String

  val materializedMetrics: Map[String, MaterializedMetric] = Map(
    "m1" -> MaterializedMetric("m1"),
    "m2" -> MaterializedMetric("m2"),
    "m3" -> MaterializedMetric("m3"),
    "m4" -> MaterializedMetric("m4")
  )
  val computedMetrics: Map[String, ComputedMetric] = Map(
    "c1" -> ComputedMetric("c1", Set(MaterializedMetric("m1"), MaterializedMetric("m2")), Set("h6")),
    "c2" -> ComputedMetric("c2", Set(MaterializedMetric("m2"), MaterializedMetric("m4"))),
    "c3" -> ComputedMetric("c3", Set(MaterializedMetric("m1")))
  )

  val metrics: Map[String, Metric] = materializedMetrics ++ computedMetrics
}
