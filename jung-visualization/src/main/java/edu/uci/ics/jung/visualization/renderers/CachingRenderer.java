package edu.uci.ics.jung.visualization.renderers;

import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

public class CachingRenderer<N, E> extends BasicRenderer<N, E> {

  protected Map<E, Shape> edgeShapeMap = new HashMap<>();
  protected Map<N, Shape> vertexShapeMap = new HashMap<>();
}
