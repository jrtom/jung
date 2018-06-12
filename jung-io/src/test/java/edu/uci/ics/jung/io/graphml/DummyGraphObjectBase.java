package edu.uci.ics.jung.io.graphml;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.function.Function;

// TODO: replace common.base.Function with java.util.Function
public class DummyGraphObjectBase {

  // TODO: hopefully we can get rid of this; could just use lambdas if we need the factory at all
  public static class UndirectedNetworkFactory
      implements Function<GraphMetadata, MutableNetwork<DummyNode, DummyEdge>> {
    public MutableNetwork<DummyNode, DummyEdge> apply(GraphMetadata arg0) {
      return NetworkBuilder.undirected().allowsParallelEdges(false).allowsSelfLoops(true).build();
    }
  }
  //    public static class UndirectedSparseGraphFactory implements Function<GraphMetadata,
  // Hypergraph<DummyNode, DummyEdge>> {
  //
  //        public Hypergraph<DummyNode, DummyEdge> apply(GraphMetadata arg0) {
  //            return new UndirectedSparseGraph<DummyNode, DummyEdge>();
  //        }
  //    }
  //
  //    public static class SetHypergraphFactory implements Function<GraphMetadata,
  // Hypergraph<DummyNode, DummyEdge>> {
  //
  //        public Hypergraph<DummyNode, DummyEdge> apply(GraphMetadata arg0) {
  //            return new SetHypergraph<DummyNode, DummyEdge>();
  //        }
  //    }

  public int myValue;

  public DummyGraphObjectBase() {}

  public DummyGraphObjectBase(int v) {
    myValue = v;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + myValue;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DummyGraphObjectBase other = (DummyGraphObjectBase) obj;
    return myValue == other.myValue;
  }
}
