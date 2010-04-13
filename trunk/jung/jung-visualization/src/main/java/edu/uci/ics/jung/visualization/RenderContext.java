package edu.uci.ics.jung.visualization;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public interface RenderContext<V, E> {

    float[] dotting = {1.0f, 3.0f};
    float[] dashing = {5.0f};

    /**
     * A stroke for a dotted line: 1 pixel width, round caps, round joins, and an 
     * array of {1.0f, 3.0f}.
     */
    Stroke DOTTED = new BasicStroke(1.0f,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dotting, 0f);

    /**
     * A stroke for a dashed line: 1 pixel width, square caps, beveled joins, and an
     * array of {5.0f}.
     */
    Stroke DASHED = new BasicStroke(1.0f,
            BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, dashing, 0f);

    /**
     * Specifies the offset for the edge labels.
     */
    int LABEL_OFFSET = 10;

    int getLabelOffset();
    
    void setLabelOffset(int labelOffset);
    
    float getArrowPlacementTolerance();

    void setArrowPlacementTolerance(float arrow_placement_tolerance);

    Transformer<Context<Graph<V,E>,E>,Shape> getEdgeArrowTransformer();

    void setEdgeArrowTransformer(Transformer<Context<Graph<V,E>,E>,Shape> edgeArrowTransformer);

    Predicate<Context<Graph<V,E>,E>> getEdgeArrowPredicate() ;

    void setEdgeArrowPredicate(Predicate<Context<Graph<V,E>,E>> edgeArrowPredicate);

    Transformer<E,Font> getEdgeFontTransformer();

    void setEdgeFontTransformer(Transformer<E,Font> edgeFontTransformer);

    Predicate<Context<Graph<V,E>,E>> getEdgeIncludePredicate();

    void setEdgeIncludePredicate(Predicate<Context<Graph<V,E>,E>> edgeIncludePredicate);

    Transformer<Context<Graph<V,E>,E>,Number> getEdgeLabelClosenessTransformer();

    void setEdgeLabelClosenessTransformer(
    		Transformer<Context<Graph<V,E>,E>,Number> edgeLabelClosenessTransformer);

    EdgeLabelRenderer getEdgeLabelRenderer();

    void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer);

    Transformer<E,Paint> getEdgeFillPaintTransformer();

    void setEdgeFillPaintTransformer(Transformer<E,Paint> edgePaintTransformer);

    Transformer<E,Paint> getEdgeDrawPaintTransformer();

    void setEdgeDrawPaintTransformer(Transformer<E,Paint> edgeDrawPaintTransformer);

    Transformer<E,Paint> getArrowDrawPaintTransformer();

    void setArrowDrawPaintTransformer(Transformer<E,Paint> arrowDrawPaintTransformer);

    Transformer<E,Paint> getArrowFillPaintTransformer();

    void setArrowFillPaintTransformer(Transformer<E,Paint> arrowFillPaintTransformer);

    Transformer<Context<Graph<V,E>,E>,Shape> getEdgeShapeTransformer();

    void setEdgeShapeTransformer(Transformer<Context<Graph<V,E>,E>,Shape> edgeShapeTransformer);

    Transformer<E,String> getEdgeLabelTransformer();

    void setEdgeLabelTransformer(Transformer<E,String> edgeStringer);

    Transformer<E,Stroke> getEdgeStrokeTransformer();

    void setEdgeStrokeTransformer(Transformer<E,Stroke> edgeStrokeTransformer);
    
    Transformer<E,Stroke> getEdgeArrowStrokeTransformer();

    void setEdgeArrowStrokeTransformer(Transformer<E,Stroke> edgeArrowStrokeTransformer);
    
    GraphicsDecorator getGraphicsContext();
    
    void setGraphicsContext(GraphicsDecorator graphicsContext);

    EdgeIndexFunction<V, E> getParallelEdgeIndexFunction();

    void setParallelEdgeIndexFunction(
            EdgeIndexFunction<V, E> parallelEdgeIndexFunction);

    PickedState<E> getPickedEdgeState();

    void setPickedEdgeState(PickedState<E> pickedEdgeState);

    PickedState<V> getPickedVertexState();

    void setPickedVertexState(PickedState<V> pickedVertexState);

    CellRendererPane getRendererPane();

    void setRendererPane(CellRendererPane rendererPane);

    JComponent getScreenDevice();

    void setScreenDevice(JComponent screenDevice);

    Transformer<V,Font> getVertexFontTransformer();

    void setVertexFontTransformer(Transformer<V,Font> vertexFontTransformer);

    Transformer<V,Icon> getVertexIconTransformer();

    void setVertexIconTransformer(Transformer<V,Icon> vertexIconTransformer);

    Predicate<Context<Graph<V,E>,V>> getVertexIncludePredicate();

    void setVertexIncludePredicate(Predicate<Context<Graph<V,E>,V>> vertexIncludePredicate);

    VertexLabelRenderer getVertexLabelRenderer();

    void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer);

    Transformer<V,Paint> getVertexFillPaintTransformer();

    void setVertexFillPaintTransformer(Transformer<V,Paint> vertexFillPaintTransformer);

    Transformer<V,Paint> getVertexDrawPaintTransformer();

    void setVertexDrawPaintTransformer(Transformer<V,Paint> vertexDrawPaintTransformer);

    Transformer<V,Shape> getVertexShapeTransformer();

    void setVertexShapeTransformer(Transformer<V,Shape> vertexShapeTransformer);

    Transformer<V,String> getVertexLabelTransformer();

    void setVertexLabelTransformer(Transformer<V,String> vertexStringer);

    Transformer<V,Stroke> getVertexStrokeTransformer();

    void setVertexStrokeTransformer(Transformer<V,Stroke> vertexStrokeTransformer);

//    MutableTransformer getViewTransformer();

//    void setViewTransformer(MutableTransformer viewTransformer);
    
    class DirectedEdgeArrowPredicate<V,E> 
    	implements Predicate<Context<Graph<V,E>,E>> {

        public boolean evaluate(Context<Graph<V,E>,E> c) {
            return c.graph.getEdgeType(c.element) == EdgeType.DIRECTED;
        }
        
    }
    
    class UndirectedEdgeArrowPredicate<V,E> 
    	implements Predicate<Context<Graph<V,E>,E>> {
    	//extends AbstractGraphPredicate<V,E> {

        public boolean evaluate(Context<Graph<V,E>,E> c) {
            return c.graph.getEdgeType(c.element) == EdgeType.UNDIRECTED;
        }
        
    }
    
    MultiLayerTransformer getMultiLayerTransformer();
    
    void setMultiLayerTransformer(MultiLayerTransformer basicTransformer);
    
	/**
	 * @return the pickSupport
	 */
	GraphElementAccessor<V, E> getPickSupport();

	/**
	 * @param pickSupport the pickSupport to set
	 */
	void setPickSupport(GraphElementAccessor<V, E> pickSupport);
	

}