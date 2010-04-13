package edu.uci.ics.jung.visualization.spatial;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;

/** 
 * maintains caches of vertices and edges that will be the subset of the
 * delegate graph's elements that are contained in some Rectangle.
 * 
 * @author tanelso
 *
 * @param <V>
 * @param <E>
 */
public class FastRenderingGraph<V, E> implements Graph<V, E> {

	protected Graph<V,E> graph;
	protected Set<V> vertices = new HashSet<V>();
	protected Set<E> edges = new HashSet<E>();
	protected boolean dirty;
	protected Set<Rectangle2D> bounds;
	protected RenderContext<V,E> rc;
	protected BasicVisualizationServer<V,E> vv;
	protected Layout<V,E> layout;
	
	public FastRenderingGraph(Graph<V,E> graph, Set<Rectangle2D> bounds, BasicVisualizationServer<V,E> vv) {
		this.graph = graph;
		this.bounds = bounds;
		this.vv = vv;
		this.rc = vv.getRenderContext();
	}
	
	private void cleanUp() {
		vertices.clear();
		edges.clear();
		for(V v : graph.getVertices()) {
			checkVertex(v);
		}
		for(E e : graph.getEdges()) {
			checkEdge(e);
		}
	}
	
	private void checkVertex(V v) {
        // get the shape to be rendered
        Shape shape = rc.getVertexShapeTransformer().apply(v);
        Point2D p = layout.apply(v);
        p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float)p.getX();
        float y = (float)p.getY();
        // create a transform that translates to the location of
        // the vertex to be rendered
        AffineTransform xform = AffineTransform.getTranslateInstance(x,y);
        // transform the vertex shape with xtransform
        shape = xform.createTransformedShape(shape);
        for(Rectangle2D r : bounds) {
        	if(shape.intersects(r)) {
        		vertices.add(v);
        	}
        }
	}
	
	private void checkEdge(E e) {
        Pair<V> endpoints = graph.getEndpoints(e);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        
        Point2D p1 = layout.apply(v1);
        Point2D p2 = layout.apply(v2);
        p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
        p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
        float x1 = (float) p1.getX();
        float y1 = (float) p1.getY();
        float x2 = (float) p2.getX();
        float y2 = (float) p2.getY();
        
        boolean isLoop = v1.equals(v2);
        Shape s2 = rc.getVertexShapeTransformer().apply(v2);
        Shape edgeShape = rc.getEdgeShapeTransformer().apply(Context.<Graph<V,E>,E>getInstance(graph, e));

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);
        
        if(isLoop) {
            // this is a self-loop. scale it is larger than the vertex
            // it decorates and translate it so that its nadir is
            // at the center of the vertex.
            Rectangle2D s2Bounds = s2.getBounds2D();
            xform.scale(s2Bounds.getWidth(),s2Bounds.getHeight());
            xform.translate(0, -edgeShape.getBounds2D().getWidth()/2);
        } else if(rc.getEdgeShapeTransformer() instanceof EdgeShape.Orthogonal) {
            float dx = x2-x1;
            float dy = y2-y1;
            int index = 0;
            if(rc.getEdgeShapeTransformer() instanceof IndexedRendering) {
            	EdgeIndexFunction<V,E> peif = 
            		((IndexedRendering<V,E>)rc.getEdgeShapeTransformer()).getEdgeIndexFunction();
            	index = peif.getIndex(graph, e);
            	index *= 20;
            }
            GeneralPath gp = new GeneralPath();
            gp.moveTo(0,0);// the xform will do the translation to x1,y1
            if(x1 > x2) {
            	if(y1 > y2) {
            		gp.lineTo(0, index);
            		gp.lineTo(dx-index, index);
            		gp.lineTo(dx-index, dy);
            		gp.lineTo(dx, dy);
            	} else {
            		gp.lineTo(0, -index);
            		gp.lineTo(dx-index, -index);
            		gp.lineTo(dx-index, dy);
            		gp.lineTo(dx, dy);
            	}

            } else {
            	if(y1 > y2) {
            		gp.lineTo(0, index);
            		gp.lineTo(dx+index, index);
            		gp.lineTo(dx+index, dy);
            		gp.lineTo(dx, dy);
            		
            	} else {
            		gp.lineTo(0, -index);
            		gp.lineTo(dx+index, -index);
            		gp.lineTo(dx+index, dy);
            		gp.lineTo(dx, dy);
            		
            	}
            	
            }

            edgeShape = gp;
        	
        } else {
            // this is a normal edge. Rotate it to the angle between
            // vertex endpoints, then scale it to the distance between
            // the vertices
            float dx = x2-x1;
            float dy = y2-y1;
            float thetaRadians = (float) Math.atan2(dy, dx);
            xform.rotate(thetaRadians);
            float dist = (float) Math.sqrt(dx*dx + dy*dy);
            xform.scale(dist, 1.0);
        }
        
        edgeShape = xform.createTransformedShape(edgeShape);
        for(Rectangle2D r : bounds) {
        	if(edgeShape.intersects(r)) {
        		edges.add(e);
        	}
        }
	}

	public Set<Rectangle2D> getBounds() {
		return bounds;
	}

	public void setBounds(Set<Rectangle2D> bounds) {
		this.bounds = bounds;
	}

	public boolean addEdge(E edge, Collection<? extends V> vertices,
			EdgeType edgeType) {
		return graph.addEdge(edge, vertices, edgeType);
	}
	public boolean addEdge(E edge, Collection<? extends V> vertices) {
		return graph.addEdge(edge, vertices);
	}
	public boolean addEdge(E e, V v1, V v2, EdgeType edgeType) {
		return graph.addEdge(e, v1, v2, edgeType);
	}
	public boolean addEdge(E e, V v1, V v2) {
		return graph.addEdge(e, v1, v2);
	}
	public boolean addVertex(V vertex) {
		return graph.addVertex(vertex);
	}
	public boolean containsEdge(E edge) {
		return graph.containsEdge(edge);
	}
	public boolean containsVertex(V vertex) {
		return graph.containsVertex(vertex);
	}
	public int degree(V vertex) {
		return graph.degree(vertex);
	}
	public E findEdge(V v1, V v2) {
		return graph.findEdge(v1, v2);
	}
	public Collection<E> findEdgeSet(V v1, V v2) {
		return graph.findEdgeSet(v1, v2);
	}
	public EdgeType getDefaultEdgeType() {
		return graph.getDefaultEdgeType();
	}
	public V getDest(E directedEdge) {
		return graph.getDest(directedEdge);
	}
	public int getEdgeCount() {
		return graph.getEdgeCount();
	}
	public int getEdgeCount(EdgeType edgeType) {
		return graph.getEdgeCount(edgeType);
	}
	public Collection<E> getEdges() {
		if(dirty) {
			cleanUp();
		}
		return edges;
	}
	public Collection<E> getEdges(EdgeType edgeType) {
		return graph.getEdges(edgeType);
	}
	public EdgeType getEdgeType(E edge) {
		return graph.getEdgeType(edge);
	}
	public Pair<V> getEndpoints(E edge) {
		return graph.getEndpoints(edge);
	}
	public int getIncidentCount(E edge) {
		return graph.getIncidentCount(edge);
	}
	public Collection<E> getIncidentEdges(V vertex) {
		return graph.getIncidentEdges(vertex);
	}
	public Collection<V> getIncidentVertices(E edge) {
		return graph.getIncidentVertices(edge);
	}
	public Collection<E> getInEdges(V vertex) {
		return graph.getInEdges(vertex);
	}
	public int getNeighborCount(V vertex) {
		return graph.getNeighborCount(vertex);
	}
	public Collection<V> getNeighbors(V vertex) {
		return graph.getNeighbors(vertex);
	}
	public V getOpposite(V vertex, E edge) {
		return graph.getOpposite(vertex, edge);
	}
	public Collection<E> getOutEdges(V vertex) {
		return graph.getOutEdges(vertex);
	}
	public int getPredecessorCount(V vertex) {
		return graph.getPredecessorCount(vertex);
	}
	public Collection<V> getPredecessors(V vertex) {
		return graph.getPredecessors(vertex);
	}
	public V getSource(E directedEdge) {
		return graph.getSource(directedEdge);
	}
	public int getSuccessorCount(V vertex) {
		return graph.getSuccessorCount(vertex);
	}
	public Collection<V> getSuccessors(V vertex) {
		return graph.getSuccessors(vertex);
	}
	public int getVertexCount() {
		return graph.getVertexCount();
	}
	public Collection<V> getVertices() {
		if(dirty) cleanUp();
		return vertices;
	}
	public int inDegree(V vertex) {
		return graph.inDegree(vertex);
	}
	public boolean isDest(V vertex, E edge) {
		return graph.isDest(vertex, edge);
	}
	public boolean isIncident(V vertex, E edge) {
		return graph.isIncident(vertex, edge);
	}
	public boolean isNeighbor(V v1, V v2) {
		return graph.isNeighbor(v1, v2);
	}
	public boolean isPredecessor(V v1, V v2) {
		return graph.isPredecessor(v1, v2);
	}
	public boolean isSource(V vertex, E edge) {
		return graph.isSource(vertex, edge);
	}
	public boolean isSuccessor(V v1, V v2) {
		return graph.isSuccessor(v1, v2);
	}
	public int outDegree(V vertex) {
		return graph.outDegree(vertex);
	}
	public boolean removeEdge(E edge) {
		return graph.removeEdge(edge);
	}
	public boolean removeVertex(V vertex) {
		return graph.removeVertex(vertex);
	}
}
