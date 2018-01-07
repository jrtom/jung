package edu.uci.ics.jung.io.graphml;

import java.util.function.Function;

public class DummyNode extends DummyGraphObjectBase {

  public static class Factory implements Function<NodeMetadata, DummyNode> {
    int n = 0;

    public DummyNode apply(NodeMetadata md) {
      return new DummyNode(n++);
    }
  }

  public DummyNode() {}

  public DummyNode(int v) {
    super(v);
  }
}
