package edu.uci.ics.jung.algorithms.generators.random;


import junit.framework.Assert;
import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.generators.TestLattice2D;
import edu.uci.ics.jung.graph.Graph;


/**
 * 
 * @author Joshua O'Madadhain
 */
public class TestKleinberg extends TestLattice2D {
	
    @Override
    protected Lattice2DGenerator<String, Number> generate(int i, int j, int k)
    {
        return new KleinbergSmallWorldGenerator<String,Number>(
                k == 0 ? undirectedGraphFactory : directedGraphFactory, 
                vertexFactory, edgeFactory,
                i, // rows
                i, // columns
                0.1, // clustering exponent
                j == 0 ? true : false); // toroidal?
    }
    
    @Override
    protected void checkEdgeCount(Lattice2DGenerator<String, Number> generator,
    	Graph<String, Number> graph) 
    
    {
        Assert.assertEquals(
        	generator.getGridEdgeCount() +
                ((KleinbergSmallWorldGenerator<?, ?>)generator).getConnectionCount()
                	* graph.getVertexCount(), 
            graph.getEdgeCount());
    }
}
