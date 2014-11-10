package com.olap

import com.olap.Constants.{ComputedMetric, MaterializedMetric}
import com.olap.Schema.FactTable

class Planner(query: Query)(implicit schema: Schema) {
  def printAll = {
    printQuery
    printPotentialFactTables
    printSupportedBreakdowns
  }

  /**
   * Compute applicable fact tables for each metric by applying local metric requirements
   */
  def getSupportedBreakdowns: Map[String, Set[Set[FactTable]]] = {
    Constants.metrics
      .mapValues {
        case metric: MaterializedMetric => potentialFactTables.filter(_.metrics.contains(metric)).map(Set(_))
        case computedMetric: ComputedMetric => {
          findMinimalSubsets[FactTable](potentialFactTables, (factTables: Set[FactTable]) => {
            val metricsFromDifferentFactTables = factTables.flatMap(_.metrics)

            factTables.forall{factTable => computedMetric.dimensionDependencies.subsetOf(factTable.allDimensions.toSet)} &&
            computedMetric.metrics.forall(metricsFromDifferentFactTables.contains)
          })
        }
      }
  }

  def printQuery = println(s"Query: $query"); println

  def printPotentialFactTables = {
    println("Potential fact tables:")
    potentialFactTables.foreach(ft => println("  * " + toStringExtended(ft)))
    println
  }

  def printSupportedBreakdowns = {
    getSupportedBreakdowns foreach { case (m, factTableSets) =>
      println("--------------------")
      print(s"$m ->")
      factTableSets foreach { factTableSet =>
        println("\t* [" + factTableSet.map { ft => toStringExtended(ft)} + "]")
      }
      if (factTableSets.isEmpty) println("\t/")
    }
  }

  /**
   * Compute potentially applicable fact tables by applying global requirements (filters, dimensions)
   */
  private def potentialFactTables: Set[FactTable] = schema.factTables
    .filter(factTable => factTable.neededFilters.forall(query.filters.contains))               // filters
    .filter(factTable => query.dimensions.forall(factTable.allDimensions.contains))            // dimensions
    .toSet

  /**
   * Given initialCandidates and function isSufficient, compute all subsets of initialCandidates, such that
   * each subset is the minimal subset which satisfies isSufficient.
   *
   * @param initialCandidates   Initial candidates that this function should search over.
   * @param isSufficient        Compute whether a particular Set[A] is sufficient.
   * @tparam A
   * @return
   */
  private def findMinimalSubsets[A](initialCandidates: Set[A], isSufficient: Set[A] => Boolean): Set[Set[A]] = {
    def _findSufficientSets(sufficientSets: Set[Set[A]], candidates: Set[Set[A]]): Set[Set[A]] = {
      val nonSuperSets: Set[Set[A]] = for (a <- candidates; b <- candidates; newSet = a ++ b; if !sufficientSets.exists(_.subsetOf(newSet))) yield newSet
      val (newSufficient, newCandidates) = nonSuperSets.partition(isSufficient)

      if (newSufficient.isEmpty) sufficientSets
      else _findSufficientSets(sufficientSets ++ newSufficient, newCandidates)
    }

    val (sufficientSets, candidates) = initialCandidates.map(Set(_)).partition(isSufficient)
    _findSufficientSets(sufficientSets, candidates)
  }

  private def toStringExtended(factTable: FactTable)(implicit schema: Schema): String =
    s"FactTable(${factTable.name}, " +
    s"[${factTable.dimensions.mkString(",")}], " +
    s"[${factTable.metrics.mkString(",")}], ${if (factTable.neededFilters.nonEmpty) "defined at: [" + factTable.neededFilters.mkString(",") + "]," else ""}" +
    s"[${factTable.joinableDimensionTables.mkString(", ")}])"

}
