package edu.uci.ics.jung.io.graphml;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class DummyGraphObjectBase {
    
    public static class UndirectedSparseGraphFactory implements Transformer<GraphMetadata, Hypergraph<DummyVertex, DummyEdge>> {
    
        public Hypergraph<DummyVertex, DummyEdge> transform(GraphMetadata arg0) {
            return new UndirectedSparseGraph<DummyVertex, DummyEdge>();
        }
    }
    
    public static class SetHypergraphFactory implements Transformer<GraphMetadata, Hypergraph<DummyVertex, DummyEdge>> {
        
        public Hypergraph<DummyVertex, DummyEdge> transform(GraphMetadata arg0) {
            return new SetHypergraph<DummyVertex, DummyEdge>();
        }
    }

    public int myValue;

    public DummyGraphObjectBase() {
    }

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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DummyGraphObjectBase other = (DummyGraphObjectBase) obj;
        return myValue == other.myValue;
    }
}
