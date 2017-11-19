package edu.uci.ics.jung.visualization.spatial;

import edu.uci.ics.jung.layout.model.LayoutModel;
import java.awt.geom.Point2D;

/**
 * @author Tom Nelson
 * @param <N> the node tyoe
 */
public abstract class AbstractSpatial<N> implements Spatial<N> {

  /** the layoutModel that the stucture operates on */
  protected LayoutModel<N, Point2D> layoutModel;

  protected AbstractSpatial(LayoutModel<N, Point2D> layoutModel) {
    this.layoutModel = layoutModel;
  }
}
