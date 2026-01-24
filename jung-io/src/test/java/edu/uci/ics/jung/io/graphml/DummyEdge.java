package edu.uci.ics.jung.io.graphml;

import java.util.function.Function;

public class DummyEdge extends DummyGraphObjectBase {

  public static class EdgeFactory implements Function<EdgeMetadata, DummyEdge> {
    int n = 100;

    public DummyEdge apply(EdgeMetadata md) {
      return new DummyEdge(n++);
    }
  }

  public DummyEdge() {}

  public DummyEdge(int v) {
    super(v);
  }
}
