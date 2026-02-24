package edu.uci.ics.jung.io.graphml

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import java.util.function.Function

open class DummyGraphObjectBase {

  // TODO: hopefully we can get rid of this; could just use lambdas if we need the factory at all
  class UndirectedNetworkFactory : Function<GraphMetadata, MutableNetwork<DummyNode, DummyEdge>> {
    override fun apply(arg0: GraphMetadata): MutableNetwork<DummyNode, DummyEdge> {
      return NetworkBuilder.undirected().allowsParallelEdges(false).allowsSelfLoops(true).build()
    }
  }

  var myValue: Int = 0

  constructor()

  constructor(v: Int) {
    myValue = v
  }

  override fun hashCode(): Int {
    val prime = 31
    var result = 1
    result = prime * result + myValue
    return result
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null) {
      return false
    }
    if (javaClass != other.javaClass) {
      return false
    }
    other as DummyGraphObjectBase
    return myValue == other.myValue
  }
}
