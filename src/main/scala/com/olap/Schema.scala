package com.olap
import Constants._

object Schema {
  trait Table {
    def name: String
    def dimensions: Seq[Dimension]
    def canJoinDimensionTable(table: DimensionTable) = this.dimensions.contains(table.dimensions.head)
  }

  case class FactTable(
    name: String,
    dimensions: Seq[Dimension],
    metrics: Set[MaterializedMetric],
    neededFilters: Set[Filter] = Set() // needed for StatusCube
  ) extends Table {
    def joinableDimensionTables(implicit schema: Schema): Seq[DimensionTable] =
      schema.dimensionTables.filter(canJoinDimensionTable)

    def allDimensions(implicit schema: Schema) = dimensions ++ joinableDimensionTables(schema).flatMap(_.dimensions)
  }

  // first dimension is PK
  case class DimensionTable(
    name: String,
    dimensions: Seq[Dimension]
  ) extends Table {
    override def toString = s"$name([${dimensions.mkString(",")}])"
  }
}

class Schema {
  import Schema._

  val dimensionTables = Seq(
    DimensionTable("DTable1", Seq("d2", "h1", "h2", "h3")),
    DimensionTable("DTable2", Seq("d4", "h5", "h6", "h7"))
  )

  val factTables = Seq(
    FactTable(
      "StatusCube1",
      Seq("d1", "d2", "d3", "d4", "d5"),
      Set("m1", "m2"),
      Set("d1 > a")
    ),
    FactTable(
      "Cube2",
      Seq("d2", "d3", "d4"),
      Set("m1", "m2", "m3")
    ),
    FactTable(
      "Cube3",
      Seq("d3", "d4", "d5"),
      Set("m3", "m4")
    ),
    FactTable(
      "Cube4",
      Seq("d1", "d2", "d3", "d4"),
      Set("m1")
    ),
    FactTable(
      "Cube5",
      Seq("d1", "d2", "d3", "d4"),
      Set("m2")
    )
  )
}
