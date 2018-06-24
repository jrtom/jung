package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BoundingRectanglePaintable<N> implements VisualizationServer.Paintable {

  protected RenderContext rc;
  protected Network graph;
  protected LayoutModel<N> layoutModel;
  protected List<Rectangle2D> rectangles;

  public BoundingRectanglePaintable(RenderContext rc, VisualizationModel<N, ?> visualizationModel) {
    super();
    this.rc = rc;
    this.layoutModel = visualizationModel.getLayoutModel();
    this.graph = visualizationModel.getNetwork();
    final BoundingRectangleCollector.Nodes<N> brc =
        new BoundingRectangleCollector.Nodes<>(rc, visualizationModel);
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
      g2d.draw(rc.getMultiLayerTransformer().transform(MultiLayerTransformer.Layer.LAYOUT, r));
    }
  }

  public boolean useTransform() {
    return true;
  }
}
