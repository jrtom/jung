package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.layout.LayoutChangeListener;
import edu.uci.ics.jung.visualization.layout.LayoutEvent;
import edu.uci.ics.jung.visualization.layout.LayoutEventSupport;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.awt.Shape;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CachingVertexRenderer extends BasicVertexRenderer
    implements ChangeListener, LayoutChangeListener {

  protected Map<Object, Shape> vertexShapeMap = new HashMap<Object, Shape>();

  protected Set<Object> dirtyVertices = new HashSet<Object>();

  @SuppressWarnings({"rawtypes", "unchecked"})
  public CachingVertexRenderer(BasicVisualizationServer vv) {
    vv.getRenderContext().getMultiLayerTransformer().addChangeListener(this);
    Layout<Object> layout = vv.getGraphLayout();
    if (layout instanceof LayoutEventSupport) {
      ((LayoutEventSupport) layout).addLayoutChangeListener(this);
    }
  }

  /** Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>. */
  protected void paintIconForVertex(
      RenderContext renderContext, LayoutMediator layoutMediator, Object v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    boolean vertexHit = true;
    int[] coords = new int[2];
    Shape shape = vertexShapeMap.get(v);
    if (shape == null || dirtyVertices.contains(v)) {
      shape = prepareFinalVertexShape(renderContext, layoutMediator, v, coords);
      vertexShapeMap.put(v, shape);
      dirtyVertices.remove(v);
    }
    vertexHit = vertexHit(renderContext, layoutMediator, shape);
    if (vertexHit) {
      if (renderContext.getVertexIconTransformer() != null) {
        Icon icon = renderContext.getVertexIconTransformer().apply(v);
        if (icon != null) {

          g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);

        } else {
          paintShapeForVertex(renderContext, layoutMediator, v, shape);
        }
      } else {
        paintShapeForVertex(renderContext, layoutMediator, v, shape);
      }
    }
  }

  public void stateChanged(ChangeEvent evt) {
    vertexShapeMap.clear();
  }

  public void layoutChanged(LayoutEvent evt) {
    this.dirtyVertices.add(evt.getVertex());
  }
}
