package edu.uci.ics.jung.io.graphml;

import org.apache.commons.collections15.Transformer;

public class DummyEdge extends DummyGraphObjectBase {
    
    public static class EdgeFactory implements Transformer<EdgeMetadata, DummyEdge> {
        int n = 100;

        public DummyEdge transform(EdgeMetadata md) {
            return new DummyEdge(n++);
        }
    }
    
    public static class HyperEdgeFactory implements Transformer<HyperEdgeMetadata, DummyEdge> {
        int n = 0;

        public DummyEdge transform(HyperEdgeMetadata md) {
            return new DummyEdge(n++);
        }
    }
    
    public DummyEdge() {
    }

    public DummyEdge(int v) {
        super(v);
    }
}
