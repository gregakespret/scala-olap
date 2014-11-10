package com.olap

object App {
  def main(args: Array[String]): Unit = {
    val query = Query(
      metrics = Set(Constants.computedMetrics("c1"), "m3"),
      dimensions = Set("d2", "d3"),
      filters = Set("d1 > a")
    )

    val planner = new Planner(query)(new Schema())
    planner.printAll
    planner.getSupportedBreakdowns
  }
}
