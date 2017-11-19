package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.util.Caching;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that includes a SpatialQuadTree data structure. This class uses
 * java.awt.geom.Point2D and java2d in order to intersect shapes and determine the content of the
 * spatial grid.
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public class SpatialQuadTreeLayoutModel<N> extends LoadingCacheLayoutModel<N, Point2D>
    implements LayoutModel<N, Point2D>, Caching {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTreeLayoutModel.class);

  /** the spatial structure to use */
  protected Spatial<N> spatial;

  public SpatialQuadTreeLayoutModel(
      Graph<N> graph, int width, int height, Function<N, Point2D> initializer) {
    super(graph, new AWTPointModel(), width, height, 0, initializer);
    setupSpatial(new Dimension(width, height));
  }

  public SpatialQuadTreeLayoutModel(Graph<N> graph, int width, int height) {
    super(graph, new AWTPointModel(), width, height, 0);
    setupSpatial(new Dimension(width, height));
  }

  /**
   * allow the passed layoutAlgorithm to visit this
   *
   * @param layoutAlgorithm the layoutAlgorithm visitor
   */
  @Override
  public void accept(LayoutAlgorithm<N, Point2D> layoutAlgorithm) {
    super.accept(layoutAlgorithm);
    spatial.recalculate(graph.nodes());
  }

  public void setSpatial(Spatial<N> spatial) {
    this.spatial = spatial;
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    if (spatial == null) {
      setupSpatial(new Dimension(width, height));
    } else if (spatial.getLayoutArea().getWidth() < width
        || spatial.getLayoutArea().getHeight() < height) {
      setupSpatial(new Dimension(width, height));
    } else {
      spatial.recalculate(this.graph.nodes());
    }
  }

  protected void setupSpatial(Dimension delegateSize) {
    this.spatial = new SpatialQuadTree(this, delegateSize.getWidth(), delegateSize.getHeight());
  }

  @Override
  public void set(N node, Point2D location, boolean forceUpdate) {
    super.set(node, location);
    if (forceUpdate) {
      log.trace("put " + node + " in " + location);
      spatial.update(node);
    }
  }

  @Override
  public void set(N node, double x, double y) {
    this.set(node, pointModel.newPoint(x, y), true);
  }

  public Spatial<N> getSpatial() {
    return this.spatial;
  }
}
