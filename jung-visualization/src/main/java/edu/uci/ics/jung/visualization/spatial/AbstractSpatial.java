package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.EvictingQueue;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor;
import java.awt.*;
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

  /** the layoutModel that the structure operates on */
  protected LayoutModel<NT> layoutModel;

  RadiusNetworkNodeAccessor<NT> fallback;

  protected AbstractSpatial(LayoutModel<NT> layoutModel) {
    this.layoutModel = layoutModel;
    if (layoutModel != null) {
      this.rectangle =
          new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight());
      this.fallback = new RadiusNetworkNodeAccessor();
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

  protected NT getClosest(Collection<NT> nodes, double x, double y, double radius) {

    // since I am comparing with distance squared, i need to square the radius
    double radiusSq = radius * radius;
    if (nodes.size() > 0) {
      double closestSoFar = Double.MAX_VALUE;
      NT winner = null;
      double winningDistance = -1;
      for (NT node : nodes) {
        Point loc = layoutModel.apply(node);
        double dist = loc.distanceSquared(x, y);

        // consider only nodes that are inside the search radius
        // and are closer than previously found nodes
        if (dist < radiusSq && dist < closestSoFar) {
          closestSoFar = dist;
          winner = node;
          winningDistance = dist;
        }
      }
      if (log.isTraceEnabled()) {
        log.trace("closest winner is {} at distance {}", winner, winningDistance);
      }
      return winner;
    } else {
      return null;
    }
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
