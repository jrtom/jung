package edu.uci.ics.jung.visualization.spatial;

import static edu.uci.ics.jung.visualization.layout.AWT.POINT_MODEL;

import com.google.common.collect.EvictingQueue;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> the element type that is managed by the spatial data structure. Node or Edge
 * @param <NT> the node type for the LayoutModel reference. Could be the same as T.
 */
public abstract class AbstractSpatial<T, NT> implements Spatial<T> {

  private static Logger log = LoggerFactory.getLogger(AbstractSpatial.class);
  /** should this model actively update itself */
  boolean active = false;

  protected Rectangle2D rectangle;

  protected Collection<Shape> pickShapes = EvictingQueue.create(4);

  /** a memoization of the grid rectangles used for rendering as Paintables for debugging */
  protected List<Shape> gridCache;

  /** the layoutModel that the stucture operates on */
  protected LayoutModel<NT, Point2D> layoutModel;

  RadiusNetworkNodeAccessor fallback;

  protected AbstractSpatial(LayoutModel<NT, Point2D> layoutModel) {
    this.layoutModel = layoutModel;
    if (layoutModel != null) {
      this.rectangle =
          new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight());
      this.fallback = new RadiusNetworkNodeAccessor(layoutModel.getGraph(), POINT_MODEL);
    }
  }

  public Collection<Shape> getPickShapes() {
    return pickShapes;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void setActive(boolean active) {
    gridCache = null;
    this.active = active;
  }

  @Override
  public void layoutStateChanged(LayoutModel.LayoutStateChangeEvent evt) {
    // if the layoutmodel is not active, then it is safe to activate this
    log.trace("layoutStateChanged:{}", evt);
    setActive(!evt.active);
    // if the layout model is finished, then rebuild the spatial data structure
    if (!evt.active) {
      log.trace("will recalcluate");
      recalculate();
      if (layoutModel instanceof LayoutModel.ChangeSupport) {
        ((LayoutModel.ChangeSupport) layoutModel).fireChanged(); // this will cause a repaint
      }
    }
  }
}
