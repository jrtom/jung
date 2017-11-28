package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.util.Caching;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree;
import java.awt.*;
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
public class SpatialQuadTreeLayoutModel<N, P> extends LoadingCacheLayoutModel<N, P>
    implements LayoutModel<N, P>, Caching {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTreeLayoutModel.class);

  /** the spatial structure to use */
  protected Spatial<N> spatial;

  public static <N, P> Builder<N, P, ?> builder() {
    return new Builder<N, P, SpatialQuadTreeLayoutModel<N, P>>() {
      @Override
      public SpatialQuadTreeLayoutModel<N, P> build() {
        return new SpatialQuadTreeLayoutModel<>(this);
      }
    };
  }

  protected SpatialQuadTreeLayoutModel(SpatialQuadTreeLayoutModel.Builder<N, P, ?> builder) {
    super(builder);
    setupSpatial(new Dimension(width, height));
  }

  /**
   * allow the passed layoutAlgorithm to visit this
   *
   * @param layoutAlgorithm the layoutAlgorithm visitor
   */
  @Override
  public void accept(LayoutAlgorithm<N, P> layoutAlgorithm) {
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
  public void set(N node, P location) {
    super.set(node, location);
    if (this.isFireEvents()) {
      log.trace("put {} in {}", node, location);
      spatial.update(node);
    }
  }

  @Override
  public void set(N node, double x, double y) {
    super.set(node, x, y);
    if (this.isFireEvents()) {
      log.trace("put {} in {},{}", node, x, y);
      spatial.update(node);
    }
  }

  public Spatial<N> getSpatial() {
    return this.spatial;
  }

  public void showLocations(String what) {
    log.info("{} locations {}", what, locations.asMap());
  }
}
