package edu.uci.ics.jung.io.graphml

import java.util.function.Function

class DummyEdge : DummyGraphObjectBase {

  class EdgeFactory : Function<EdgeMetadata, DummyEdge> {
    var n = 100

    override fun apply(md: EdgeMetadata): DummyEdge {
      return DummyEdge(n++)
    }
  }

  constructor() : super()

  constructor(v: Int) : super(v)
}
