package edu.uci.ics.jung.visualization.control;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * A plugin that can create vertices, undirected edges, and directed edges
 * using mouse gestures.
 * 
 * vertexSupport and edgeSupport member classes are responsible for actually
 * creating the new graph elements, and for repainting the view when changes
 * were made.
 * 
 * @author Tom Nelson
 *
 */
public class EditingGraphMousePlugin<V,E> extends AbstractGraphMousePlugin implements
    MouseListener, MouseMotionListener {
    
	protected VertexSupport<V,E> vertexSupport;
	protected EdgeSupport<V,E> edgeSupport;
	private Creating createMode = Creating.UNDETERMINED;
	private enum Creating { EDGE, VERTEX, UNDETERMINED }
    
    /**
     * Creates an instance and prepares shapes for visual effects, using the default modifiers
     * of BUTTON1_MASK.
     * @param vertexFactory for creating vertices
     * @param edgeFactory for creating edges
     */
    public EditingGraphMousePlugin(Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
        this(MouseEvent.BUTTON1_MASK, vertexFactory, edgeFactory);
    }

    /**
     * Creates an instance and prepares shapes for visual effects.
     * @param modifiers the mouse event modifiers to use
     * @param vertexFactory for creating vertices
     * @param edgeFactory for creating edges
     */
    public EditingGraphMousePlugin(int modifiers, Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
        super(modifiers);
		this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		this.vertexSupport = new SimpleVertexSupport<V,E>(vertexFactory);
		this.edgeSupport = new SimpleEdgeSupport<V,E>(edgeFactory);
    }
    
    /**
     * Overridden to be more flexible, and pass events with
     * key combinations. The default responds to both ButtonOne
     * and ButtonOne+Shift
     */
    @Override
    public boolean checkModifiers(MouseEvent e) {
        return (e.getModifiers() & modifiers) != 0;
    }

    /**
     * If the mouse is pressed in an empty area, create a new vertex there.
     * If the mouse is pressed on an existing vertex, prepare to create
     * an edge from that vertex to another
     */
    @SuppressWarnings("unchecked")
	public void mousePressed(MouseEvent e) {
        if(checkModifiers(e)) {
            final VisualizationViewer<V,E> vv =
                (VisualizationViewer<V,E>)e.getSource();
            final Point2D p = e.getPoint();
            GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
            if(pickSupport != null) {
                final V vertex = pickSupport.getVertex(vv.getModel().getGraphLayout(), p.getX(), p.getY());
                if(vertex != null) { // get ready to make an edge
                	this.createMode = Creating.EDGE;
                	Graph<V,E> graph = vv.getModel().getGraphLayout().getGraph();
                	// set default edge type
                	EdgeType edgeType = (graph instanceof DirectedGraph) ?
                			EdgeType.DIRECTED : EdgeType.UNDIRECTED;
                    if((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0
                    		&& graph instanceof UndirectedGraph == false) {
                        edgeType = EdgeType.DIRECTED;
                    }
                    edgeSupport.startEdgeCreate(vv, vertex, e.getPoint(), edgeType);
                } else { // make a new vertex
                	this.createMode = Creating.VERTEX;
                	vertexSupport.startVertexCreate(vv, e.getPoint());
                }
            }
        }
    }
    
    /**
     * If startVertex is non-null, and the mouse is released over an
     * existing vertex, create an undirected edge from startVertex to
     * the vertex under the mouse pointer. If shift was also pressed,
     * create a directed edge instead.
     */
    @SuppressWarnings("unchecked")
	public void mouseReleased(MouseEvent e) {
        if(checkModifiers(e)) {
            final VisualizationViewer<V,E> vv =
                (VisualizationViewer<V,E>)e.getSource();
            final Point2D p = e.getPoint();
            Layout<V,E> layout = vv.getGraphLayout();
            if(createMode == Creating.EDGE) {
                GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
                V vertex = null;
                if(pickSupport != null) {
                    vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
                }
                edgeSupport.endEdgeCreate(vv, vertex);
            } else if(createMode == Creating.VERTEX){
            	vertexSupport.endVertexCreate(vv, e.getPoint());
            }
        }
        createMode = Creating.UNDETERMINED;
    }

    /**
     * If startVertex is non-null, stretch an edge shape between
     * startVertex and the mouse pointer to simulate edge creation
     */
    @SuppressWarnings("unchecked")
    public void mouseDragged(MouseEvent e) {
        if(checkModifiers(e)) {
            VisualizationViewer<V,E> vv =
                (VisualizationViewer<V,E>)e.getSource();
            if(createMode == Creating.EDGE) {
            	edgeSupport.midEdgeCreate(vv, e.getPoint());
            } else if(createMode == Creating.VERTEX){
            	vertexSupport.midVertexCreate(vv, e.getPoint());
            }
        }
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {
        JComponent c = (JComponent)e.getSource();
        c.setCursor(cursor);
    }
    public void mouseExited(MouseEvent e) {
        JComponent c = (JComponent)e.getSource();
        c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    public void mouseMoved(MouseEvent e) {}

	public VertexSupport<V,E> getVertexSupport() {
		return vertexSupport;
	}

	public void setVertexSupport(VertexSupport<V,E> vertexSupport) {
		this.vertexSupport = vertexSupport;
	}

	public EdgeSupport<V,E> getEdgeSupport() {
		return edgeSupport;
	}

	public void setEdgeSupport(EdgeSupport<V,E> edgeSupport) {
		this.edgeSupport = edgeSupport;
	}
    
    
}
