package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.util.Caching;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialGrid;
import java.awt.*;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that includes a SpatialGrid data structure. This class uses java.awt.geom.Point2D
 * and java2d in order to intersect shapes and determine the content of the spatial grid.
 *
 * @param <N>
 */
public class SpatialGridLayoutModel<N> extends LoadingCacheLayoutModel<N, Point2D>
    implements LayoutModel<N, Point2D>, Caching {

  private static final Logger log = LoggerFactory.getLogger(SpatialGridLayoutModel.class);

  protected Spatial<N> spatial;

  public static class Builder<N> extends LoadingCacheLayoutModel.Builder<N, Point2D> {

    public SpatialGridLayoutModel<N> build() {
      return new SpatialGridLayoutModel<N>(this);
    }
    /** Returns a {@link LoadingCacheLayoutModel} instance. */
    public static <N> SpatialQuadTreeLayoutModel.Builder<N> builder() {
      return new SpatialQuadTreeLayoutModel.Builder<>();
    }
  }

  SpatialGridLayoutModel(SpatialGridLayoutModel.Builder<N> builder) {
    super(builder);
    setupSpatialGrid(new Dimension(width, height), 10, 10);
  }

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
      setupSpatialGrid(new Dimension(width, height), 10, 10);
    } else if (spatial.getLayoutArea().getWidth() < width
        || spatial.getLayoutArea().getHeight() < height) {
      setupSpatialGrid(new Dimension(width, height), 10, 10);
    } else {
      spatial.recalculate(this.graph.nodes());
    }
  }

  protected void setupSpatialGrid(Dimension delegateSize, int horizontalCount, int verticalCount) {
    this.spatial =
        new SpatialGrid(
            this,
            new Rectangle(0, 0, delegateSize.width, delegateSize.height),
            horizontalCount,
            verticalCount);
  }

  /**
   * override to default forceUpdate to true to update the spatial data structure
   *
   * @param node the node whose location is to be specified
   * @param location the coordinates of the specified location
   */
  @Override
  public void set(N node, Point2D location) {
    super.set(node, location);
    if (isFireEvents()) {
      spatial.update(node);
    }
  }

  @Override
  public void set(N node, double x, double y) {
    super.set(node, x, y);
    if (isFireEvents()) {
      spatial.update(node);
    }
  }

  public Spatial<N> getSpatial() {
    return this.spatial;
  }
}
