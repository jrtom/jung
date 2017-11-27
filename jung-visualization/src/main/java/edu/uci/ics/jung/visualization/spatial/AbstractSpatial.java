package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.EvictingQueue;
import edu.uci.ics.jung.layout.model.LayoutModel;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collection;

/**
 * @author Tom Nelson
 * @param <N> the node tyoe
 */
public abstract class AbstractSpatial<N> implements Spatial<N> {

  protected Collection<Shape> pickShapes = EvictingQueue.create(4);

  /** the layoutModel that the stucture operates on */
  protected LayoutModel<N, Point2D> layoutModel;

  protected AbstractSpatial(LayoutModel<N, Point2D> layoutModel) {
    this.layoutModel = layoutModel;
  }

  public Collection<Shape> getPickShapes() {
    return pickShapes;
  }
}
