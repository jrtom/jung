package edu.uci.ics.jung.visualization.renderers;

import java.awt.Shape;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.layout.LayoutChangeListener;
import edu.uci.ics.jung.visualization.layout.LayoutEvent;
import edu.uci.ics.jung.visualization.layout.LayoutEventSupport;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class CachingVertexRenderer<V, E> extends BasicVertexRenderer<V, E> 
	implements ChangeListener, LayoutChangeListener<V,E> {
	
	protected Map<V,Shape> vertexShapeMap = new HashMap<V,Shape>();
	
	protected Set<V> dirtyVertices = new HashSet<V>();
	
	public CachingVertexRenderer(BasicVisualizationServer<V,E> vv) {
		vv.getRenderContext().getMultiLayerTransformer().addChangeListener(this);
		Layout<V,E> layout = vv.getGraphLayout();
		if(layout instanceof LayoutEventSupport) {
			((LayoutEventSupport)layout).addLayoutChangeListener(this);
			
		}
	}
	
    /**
     * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>.
     */
    protected void paintIconForVertex(RenderContext<V,E> rc, V v, Layout<V,E> layout) {
        GraphicsDecorator g = rc.getGraphicsContext();
        boolean vertexHit = true;
        int[] coords = new int[2];
        Shape shape = vertexShapeMap.get(v);
        if(shape == null || dirtyVertices.contains(v)) {
        	shape = prepareFinalVertexShape(rc, v, layout, coords);
        	vertexShapeMap.put(v, shape);
        	dirtyVertices.remove(v);
        }
        vertexHit = vertexHit(rc, shape);
        if (vertexHit) {
        	if(rc.getVertexIconTransformer() != null) {
        		Icon icon = rc.getVertexIconTransformer().apply(v);
        		if(icon != null) {
        		
           			g.draw(icon, rc.getScreenDevice(), shape, coords[0], coords[1]);

        		} else {
        			paintShapeForVertex(rc, v, shape);
        		}
        	} else {
        		paintShapeForVertex(rc, v, shape);
        	}
        }
    }

//	@Override
	public void stateChanged(ChangeEvent evt) {
		System.err.println("got change event "+evt);
		vertexShapeMap.clear();
		
	}

//	@Override
	public void layoutChanged(LayoutEvent<V,E> evt) {
		V vertex = evt.getVertex();
		Graph<V,E> graph = evt.getGraph();
		Collection<E> edges = graph.getIncidentEdges(vertex);
		this.dirtyVertices.add(vertex);
	}


}
