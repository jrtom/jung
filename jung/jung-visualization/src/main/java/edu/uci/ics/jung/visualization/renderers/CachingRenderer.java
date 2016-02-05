package edu.uci.ics.jung.visualization.renderers;

import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

public class CachingRenderer<V,E> extends BasicRenderer<V,E> {
	
	protected Map<E,Shape> edgeShapeMap = new HashMap<E,Shape>();
	
	protected Map<V,Shape> vertexShapeMap = new HashMap<V,Shape>();
	
	

}
