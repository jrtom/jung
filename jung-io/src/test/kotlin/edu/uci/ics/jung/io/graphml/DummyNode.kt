package edu.uci.ics.jung.io.graphml

import java.util.function.Function

class DummyNode : DummyGraphObjectBase {

  class Factory : Function<NodeMetadata, DummyNode> {
    var n = 0

    override fun apply(md: NodeMetadata): DummyNode {
      return DummyNode(n++)
    }
  }

  constructor() : super()

  constructor(v: Int) : super(v)
}
