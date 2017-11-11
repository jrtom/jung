package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BoundingRectanglePaintable<V, E> implements VisualizationServer.Paintable {

  protected RenderContext<V, E> rc;
  protected Network<V, E> graph;
  protected LayoutModel<V, Point2D> layoutModel;
  protected List<Rectangle2D> rectangles;

  public BoundingRectanglePaintable(
      RenderContext<V, E> rc, VisualizationModel<V, E, Point2D> visualizationModel) {
    super();
    this.rc = rc;
    this.layoutModel = visualizationModel.getLayoutModel();
    this.graph = visualizationModel.getNetwork();
    final BoundingRectangleCollector<V, E> brc =
        new BoundingRectangleCollector<V, E>(rc, visualizationModel);
    this.rectangles = brc.getRectangles();
    if (layoutModel instanceof ChangeEventSupport) {
      ((ChangeEventSupport) layoutModel)
          .addChangeListener(
              new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                  brc.compute();
                  rectangles = brc.getRectangles();
                }
              });
    }
  }

  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g.setColor(Color.cyan);

    for (Rectangle2D r : rectangles) {
      g2d.draw(rc.getMultiLayerTransformer().transform(Layer.LAYOUT, r));
    }
  }

  public boolean useTransform() {
    return true;
  }
}
