package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.layout.LayoutChangeListener;
import edu.uci.ics.jung.visualization.layout.LayoutEvent;
import edu.uci.ics.jung.visualization.layout.LayoutEventSupport;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Shape;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CachingVertexRenderer<V, E> extends BasicVertexRenderer<V>
    implements ChangeListener, LayoutChangeListener<V> {

  protected Map<V, Shape> vertexShapeMap = new HashMap<V, Shape>();

  protected Set<V> dirtyVertices = new HashSet<V>();

  @SuppressWarnings({"rawtypes", "unchecked"})
  public CachingVertexRenderer(BasicVisualizationServer<V, E> vv) {
    super(vv.getGraphLayout(), vv.getRenderContext());
    vv.getRenderContext().getMultiLayerTransformer().addChangeListener(this);
    Layout<V> layout = vv.getGraphLayout();
    if (layout instanceof LayoutEventSupport) {
      ((LayoutEventSupport) layout).addLayoutChangeListener(this);
    }
  }

  /** Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>. */
  protected void paintIconForVertex(V v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    boolean vertexHit = true;
    int[] coords = new int[2];
    Shape shape = vertexShapeMap.get(v);
    if (shape == null || dirtyVertices.contains(v)) {
      shape = prepareFinalVertexShape(v, coords);
      vertexShapeMap.put(v, shape);
      dirtyVertices.remove(v);
    }
    vertexHit = vertexHit(shape);
    if (vertexHit) {
      if (renderContext.getVertexIconTransformer() != null) {
        Icon icon = renderContext.getVertexIconTransformer().apply(v);
        if (icon != null) {

          g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);

        } else {
          paintShapeForVertex(v, shape);
        }
      } else {
        paintShapeForVertex(v, shape);
      }
    }
  }

  public void stateChanged(ChangeEvent evt) {
    //		System.err.println("got change event "+evt);
    vertexShapeMap.clear();
  }

  public void layoutChanged(LayoutEvent<V> evt) {
    this.dirtyVertices.add(evt.getVertex());
  }
}
