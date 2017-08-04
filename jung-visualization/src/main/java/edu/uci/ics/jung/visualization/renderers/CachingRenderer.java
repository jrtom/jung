package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.RenderContext;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

public class CachingRenderer<V, E> extends BasicRenderer<V, E> {

  protected Map<E, Shape> edgeShapeMap = new HashMap<E, Shape>();
  protected Map<V, Shape> vertexShapeMap = new HashMap<V, Shape>();

  public CachingRenderer(Layout<V> layout, RenderContext<V, E> rc) {
    super(layout, rc);
  }
}
