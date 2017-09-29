package edu.uci.ics.jung.visualization.renderers;

import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

public class CachingRenderer extends BasicRenderer {

  protected Map<Object, Shape> edgeShapeMap = new HashMap<Object, Shape>();
  protected Map<Object, Shape> vertexShapeMap = new HashMap<Object, Shape>();
}
