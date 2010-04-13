package edu.uci.ics.jung.io.graphml;

import org.apache.commons.collections15.Transformer;

public class DummyVertex extends DummyGraphObjectBase {
    
    public static class Factory implements Transformer<NodeMetadata, DummyVertex> {
        int n = 0;

        public DummyVertex transform(NodeMetadata md) {
            return new DummyVertex(n++);
        }
    }
    
    public DummyVertex() {
    }

    public DummyVertex(int v) {
        super(v);
    }
}
