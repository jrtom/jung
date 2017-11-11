package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialGrid;
import edu.uci.ics.jung.visualization.util.Caching;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that includes a Spatial data structure. This class uses java.awt.geom.Point2D and
 * java2d in order to intersect shapes and determine the content of the spatial grid.
 *
 * @param <N>
 */
public class SpatialLayoutModel<N> extends LoadingCacheLayoutModel<N, Point2D>
    implements LayoutModel<N, Point2D>, Caching {

  Logger log = LoggerFactory.getLogger(SpatialLayoutModel.class);

  protected Spatial<N> spatial;

  public SpatialLayoutModel(
      Graph<N> graph, int width, int height, Function<N, Point2D> initializer) {
    super(graph, new AWTDomainModel(), width, height, initializer);
    log.info("CTOR");
    setupSpatialGrid(new Dimension(width, height), 10, 10);
  }

  public SpatialLayoutModel(Graph<N> graph, int width, int height) {
    super(graph, new AWTDomainModel(), width, height);
    setupSpatialGrid(new Dimension(width, height), 10, 10);
  }

  @Override
  public void accept(LayoutAlgorithm<N, Point2D> layoutAlgorithm) {
    super.accept(layoutAlgorithm);
    spatial.recalculate(this, graph.nodes());
  }

  public void setSpatial(Spatial<N> spatial) {
    this.spatial = spatial;
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    if (spatial == null) {
      setupSpatialGrid(new Dimension(width, height), 10, 10);
    } else if (spatial.getLayoutArea().getWidth() < width
        || spatial.getLayoutArea().getHeight() < height) {
      setupSpatialGrid(new Dimension(width, height), 10, 10);
    } else {
      spatial.recalculate(this, this.graph.nodes());
    }
  }

  protected void setupSpatialGrid(Dimension delegateSize, int horizontalCount, int verticalCount) {
    this.spatial =
        new SpatialGrid(
            new Rectangle(0, 0, delegateSize.width, delegateSize.height),
            horizontalCount,
            verticalCount);
  }

  @Override
  public void set(N node, Point2D location, boolean forceUpdate) {
    super.set(node, location);
    if (forceUpdate) {
      log.trace("put " + node + " in " + location);
      // if the node has moved outside of the layout area, recreate the spatial grid to include it
      if (!spatial.getLayoutArea().contains(location)) {
        log.info(location + " outside of spatial " + spatial.getLayoutArea());
        spatial.setBounds(spatial.getUnion(spatial.getLayoutArea(), location));
        spatial.recalculate(this, getGraph().nodes());
      } else {
        // just make sure the node is in the right box
        spatial.update(node, location);
      }
    }
  }

  @Override
  public void set(N node, double x, double y) {
    this.set(node, domainModel.newPoint(x, y));
  }

  public Spatial<N> getSpatial() {
    return this.spatial;
  }
}
