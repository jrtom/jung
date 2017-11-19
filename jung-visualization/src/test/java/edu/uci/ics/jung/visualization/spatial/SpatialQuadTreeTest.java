package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.Sets;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 * test to make sure that a search for a node returns the same leaf that you get when you search for
 * the point location of that node
 *
 * @author Tom Nelson
 */
public class SpatialQuadTreeTest {

  Set<String> nodes = Sets.newHashSet();
  int width = 600;
  int height = 600;

  @Test
  public void testRandomPointsAndLocations() {
    // generate 100 random points
    for (int i = 0; i < 100; i++) {
      nodes.add("p" + i);
    }

    Graph<String> graph = new GraphLikeThingWithNodes<>(nodes);
    LayoutModel<String, Point2D> layoutModel =
        new LoadingCacheLayoutModel(
            graph,
            new AWTPointModel(),
            width,
            height,
            new RandomLocationTransformer(
                new AWTPointModel(), width, height, 0, System.currentTimeMillis()));

    SpatialQuadTree tree = new SpatialQuadTree(layoutModel, width, height);
    for (String node : nodes) {
      tree.insert(node);
    }

    for (String node : nodes) {
      Point2D location = layoutModel.apply(node);
      SpatialQuadTree pointQuadTree = tree.getContainingQuadTreeLeaf(location);
      SpatialQuadTree nodeQuadTree = tree.getContainingQuadTreeLeaf(node);
      Assert.assertEquals(pointQuadTree, nodeQuadTree);
    }
  }

  class GraphLikeThingWithNodes<N> implements Graph<N> {
    Set<N> nodes = Sets.newHashSet();

    public GraphLikeThingWithNodes(Collection<N> nodes) {
      this.nodes.addAll(nodes);
    }

    @Override
    public Set<N> nodes() {
      return nodes;
    }

    @Override
    public Set<EndpointPair<N>> edges() {
      return null;
    }

    @Override
    public boolean isDirected() {
      return false;
    }

    @Override
    public boolean allowsSelfLoops() {
      return false;
    }

    @Override
    public ElementOrder<N> nodeOrder() {
      return null;
    }

    @Override
    public Set<N> adjacentNodes(N n) {
      return null;
    }

    @Override
    public Set<N> predecessors(N n) {
      return null;
    }

    @Override
    public Set<N> successors(N n) {
      return null;
    }

    @Override
    public int degree(N n) {
      return 0;
    }

    @Override
    public int inDegree(N n) {
      return 0;
    }

    @Override
    public int outDegree(N n) {
      return 0;
    }
  }
}
