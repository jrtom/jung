/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.transformation;

import java.util.Map;
import java.util.Set;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import edu.uci.ics.jung.algorithms.blockmodel.VertexPartition;

/**
 * This class transforms a graph with a known vertex partitioning into a graph whose 
 * vertices correspond to the input graph's partitions
 * 
 * <p>Concept based on Danyel Fisher's <code>GraphCollapser</code> in JUNG 1.x.
 */
// TODO: add tests
public class VertexPartitionCollapser 
{
    /**
     * Creates a new graph whose vertices correspond to the partitions of the supplied graph.
     * Two nodes u and v in the collapsed graph will be connected if there is an edge between
     * any of the nodes in u and any of the nodes in v, and u and v are distinct.  The value
     * of the edge represents the number of such edges.
     * @param partitioning a vertex partition of a graph
     * @return the collapsed graph 
     */
    public static <V> ValueGraph<Set<V>, Integer> collapseVertexPartitions(
    		VertexPartition<V> partitioning)
    {
        Graph<V> original = partitioning.getGraph();
        ValueGraphBuilder<Object, Object> builder = original.isDirected()
        		? ValueGraphBuilder.directed()
        		: ValueGraphBuilder.undirected();
        MutableValueGraph<Set<V>, Integer> collapsed = builder.build();
        
        // create vertices in new graph corresponding to equivalence sets in the original graph
        for (Set<V> set : partitioning.getVertexPartitions())
        {
        	collapsed.addNode(set);
        }

        // for each pair of endpoints in the original graph, connect the corresponding nodes
        // (representing partitions) in the collapsed graph if the partitions are different
        Map<V, Set<V>> nodeToPartition = partitioning.getVertexToPartitionMap();
        for (EndpointPair<V> endpoints : original.edges()) {
        	V nodeU = endpoints.nodeU();
        	V nodeV = endpoints.nodeV();
        	Set<V> partitionU = nodeToPartition.get(nodeU);
        	Set<V> partitionV = nodeToPartition.get(nodeV);
        	if (nodeU.equals(nodeV) || partitionU.equals(partitionV)) {
        		// we only connect partitions if the partitions are different;
        		// check the nodes first as an optimization
        		continue;
        	}

        	int edgeCount = collapsed.edgeValueOrDefault(partitionU, partitionV, 0);
        	collapsed.putEdgeValue(partitionU, partitionV, edgeCount + 1);
        }
        return collapsed;
    }
}
