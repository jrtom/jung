package edu.uci.ics.jung.algorithms.shortestpath;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.junit.Ignore;

import java.util.*;

/**
 * Created by Lewis Heptonstall on 06/05/2017.
 *
 * Tests demonstrate the value of capping the max_cache_size when doing many, recurrent shortest paths, calculations
 * All tests fail when max_cache_size is not set with either OutOfMemory or GC Limit Exceeded.
 * All test should pass when max_cache_size set to 100
 *
 */
public class TestCachingLargeGraph extends TestCase {

    Graph<String,Integer> ug;
    List<String> vertices;

    public void testAllPathsUnweighted(){

        setUp();

        DijkstraShortestPath<String,Integer> sp = new DijkstraShortestPath<String, Integer>(ug,true, 100);

        Map<String,Map<String,Number>> allSp = new HashMap<String, Map<String, Number>>();

        // Find shortest path from every vertex to every vertex
        for (int v1 = 0; v1 < vertices.size(); v1++) {

            for (int v2 = 0; v2 < vertices.size(); v2 ++) {
               List<Integer> thisPath = sp.getPath(vertices.get(v1),vertices.get(v2));
             }

            if (v1 % 100 == 0){ // Every 100 iterations, print progress
                System.out.println("Progress: " + new Double(v1)/vertices.size());
            }

        }

    }

    public void testDistanceMapUnweighted(){

        setUp();

        DijkstraShortestPath<String,Integer> sp = new DijkstraShortestPath<String, Integer>(ug, true, 100);

        Map<String,Map<String,Number>> allSp = new HashMap<String, Map<String, Number>>();

        // Find shortest path distance from every vertex to every vertex
        for (int v1 = 0; v1 < vertices.size(); v1++) {
            Map<String, Number> test = sp.getDistanceMap(vertices.get(v1));

            allSp.put(vertices.get(v1),test);

            if (v1 % 100 == 0){ // Every 100 iterations, print progress
                System.out.println("Progress: " + new Double(v1)/vertices.size());
            }

        }

    }

    @Override
    protected void setUp()  {

        /*
        Builds a very large, sparsely connected, unweighted, undirected graph
         */

        ug = new UndirectedSparseGraph<String,Integer>();
        vertices = new ArrayList<String>();

        int vertex_count = 4000;
        int max_connections_per_vertex = 30;
        Random random = new Random();
        random.setSeed(1234);

        // Add vertex_count number of vertices to the list, with a random ID
        for (int v = 0; v < vertex_count; v++) {
            vertices.add(((Long) random.nextLong()).toString());
            ug.addVertex(vertices.get(v));
        }

        int e = 0; // edge iterator

        for (int v1 = 0; v1 < vertices.size(); v1++) {
            for (int v2 = v1 + 1; v2 < ((v1 + max_connections_per_vertex < vertices.size()) ? (v1 + max_connections_per_vertex) : vertices.size()); v2++){

                ug.addEdge(e,vertices.get(v1),vertices.get(v2), EdgeType.UNDIRECTED);
                e++;

            }

        }

    }
}
