package edu.uci.ics.jung.io.graphml;

import com.google.common.base.Function;

public class DummyVertex extends DummyGraphObjectBase {
    
    public static class Factory implements Function<NodeMetadata, DummyVertex> {
        int n = 0;

        public DummyVertex apply(NodeMetadata md) {
            return new DummyVertex(n++);
        }
    }
    
    public DummyVertex() {
    }

    public DummyVertex(int v) {
        super(v);
    }
}
