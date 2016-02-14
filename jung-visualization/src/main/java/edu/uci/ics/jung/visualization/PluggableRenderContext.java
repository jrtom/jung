/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.DefaultParallelEdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.IncidentEdgeIndexFunction;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.DirectionalEdgeArrowTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;


public class PluggableRenderContext<V, E> implements RenderContext<V, E> {
    
	protected float arrowPlacementTolerance = 1;
    protected Predicate<Context<Graph<V,E>,V>> vertexIncludePredicate = Predicates.alwaysTrue();
    protected Function<? super V,Stroke> vertexStrokeTransformer = 
    	Functions.<Stroke>constant(new BasicStroke(1.0f));
    
    protected Function<? super V,Shape> vertexShapeTransformer = 
        		Functions.<Shape>constant(
        		new Ellipse2D.Float(-10,-10,20,20));

    protected Function<? super V,String> vertexLabelTransformer = Functions.constant(null);
    protected Function<? super V,Icon> vertexIconTransformer;
    protected Function<? super V,Font> vertexFontTransformer = 
        Functions.constant(new Font("Helvetica", Font.PLAIN, 12));
    
    protected Function<? super V,Paint> vertexDrawPaintTransformer = 
    	Functions.<Paint>constant(Color.BLACK);
    protected Function<? super V,Paint> vertexFillPaintTransformer = 
    	Functions.<Paint>constant(Color.RED);
    
    protected Function<? super E,String> edgeLabelTransformer = 
    	Functions.constant(null);
    protected Function<? super E,Stroke> edgeStrokeTransformer = 
    	Functions.<Stroke>constant(new BasicStroke(1.0f));
    protected Function<? super E,Stroke> edgeArrowStrokeTransformer = 
    	Functions.<Stroke>constant(new BasicStroke(1.0f));
    
    protected Function<? super Context<Graph<V,E>,E>,Shape> edgeArrowTransformer = 
        new DirectionalEdgeArrowTransformer<V,E>(10, 8, 4);
    
    protected Predicate<Context<Graph<V,E>,E>> edgeArrowPredicate = new DirectedEdgeArrowPredicate<V,E>();
    protected Predicate<Context<Graph<V,E>,E>> edgeIncludePredicate = Predicates.alwaysTrue();
    protected Function<? super E,Font> edgeFontTransformer =
        Functions.constant(new Font("Helvetica", Font.PLAIN, 12));
    protected Function<? super Context<Graph<V,E>,E>,Number> edgeLabelClosenessTransformer = 
        new ConstantDirectionalEdgeValueTransformer<V,E>(0.5, 0.65);
    protected Function<? super E, Shape> edgeShapeTransformer;
    protected Function<? super E,Paint> edgeFillPaintTransformer =
        Functions.constant(null);
    protected Function<? super E,Paint> edgeDrawPaintTransformer =
        Functions.<Paint>constant(Color.black);
    protected Function<? super E,Paint> arrowFillPaintTransformer =
        Functions.<Paint>constant(Color.black);
    protected Function<? super E,Paint> arrowDrawPaintTransformer =
        Functions.<Paint>constant(Color.black);
    
    protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction = 
        DefaultParallelEdgeIndexFunction.<V,E>getInstance();
    
    protected EdgeIndexFunction<V,E> incidentEdgeIndexFunction = 
        IncidentEdgeIndexFunction.<V,E>getInstance();
    
    protected MultiLayerTransformer multiLayerTransformer = new BasicTransformer();
    
	/**
	 * pluggable support for picking graph elements by
	 * finding them based on their coordinates.
	 */
	protected GraphElementAccessor<V, E> pickSupport;

    
    protected int labelOffset = LABEL_OFFSET;
    
    /**
     * the JComponent that this Renderer will display the graph on
     */
    protected JComponent screenDevice;
    
    protected PickedState<V> pickedVertexState;
    protected PickedState<E> pickedEdgeState;
    
    /**
     * The CellRendererPane is used here just as it is in JTree
     * and JTable, to allow a pluggable JLabel-based renderer for
     * Vertex and Edge label strings and icons.
     */
    protected CellRendererPane rendererPane = new CellRendererPane();
    
    /**
     * A default GraphLabelRenderer - picked Vertex labels are
     * blue, picked edge labels are cyan
     */
    protected VertexLabelRenderer vertexLabelRenderer = 
        new DefaultVertexLabelRenderer(Color.blue);
    
    protected EdgeLabelRenderer edgeLabelRenderer = new DefaultEdgeLabelRenderer(Color.cyan);
    
    protected GraphicsDecorator graphicsContext;
    
    private EdgeShape<V, E> edgeShape;
    
    PluggableRenderContext(Graph<V, E> graph) {
        this.edgeShape = new EdgeShape<V, E>(graph);
    	this.edgeShapeTransformer = edgeShape.new QuadCurve();    	
    }

	/**
	 * @return the vertexShapeTransformer
	 */
	public Function<? super V, Shape> getVertexShapeTransformer() {
		return vertexShapeTransformer;
	}

	/**
	 * @param vertexShapeTransformer the vertexShapeTransformer to set
	 */
	public void setVertexShapeTransformer(
			Function<? super V, Shape> vertexShapeTransformer) {
		this.vertexShapeTransformer = vertexShapeTransformer;
	}

	/**
	 * @return the vertexStrokeTransformer
	 */
	public Function<? super V, Stroke> getVertexStrokeTransformer() {
		return vertexStrokeTransformer;
	}

	/**
	 * @param vertexStrokeTransformer the vertexStrokeTransformer to set
	 */
	public void setVertexStrokeTransformer(
			Function<? super V, Stroke> vertexStrokeTransformer) {
		this.vertexStrokeTransformer = vertexStrokeTransformer;
	}

	public static float[] getDashing() {
        return dashing;
    }

    public static float[] getDotting() {
        return dotting;
    }

    public float getArrowPlacementTolerance() {
        return arrowPlacementTolerance;
    }

    public void setArrowPlacementTolerance(float arrow_placement_tolerance) {
        this.arrowPlacementTolerance = arrow_placement_tolerance;
    }

    public Function<? super Context<Graph<V,E>,E>,Shape> getEdgeArrowTransformer() {
        return edgeArrowTransformer;
    }

    public void setEdgeArrowTransformer(Function<? super Context<Graph<V,E>,E>,Shape> edgeArrowTransformer) {
        this.edgeArrowTransformer = edgeArrowTransformer;
    }

    public Predicate<Context<Graph<V,E>,E>> getEdgeArrowPredicate() {
        return edgeArrowPredicate;
    }

    public void setEdgeArrowPredicate(Predicate<Context<Graph<V,E>,E>> edgeArrowPredicate) {
        this.edgeArrowPredicate = edgeArrowPredicate;
    }

    public Function<? super E,Font> getEdgeFontTransformer() {
        return edgeFontTransformer;
    }

    public void setEdgeFontTransformer(Function<? super E,Font> edgeFontTransformer) {
        this.edgeFontTransformer = edgeFontTransformer;
    }

    public Predicate<Context<Graph<V,E>,E>> getEdgeIncludePredicate() {
        return edgeIncludePredicate;
    }

    public void setEdgeIncludePredicate(Predicate<Context<Graph<V,E>,E>> edgeIncludePredicate) {
        this.edgeIncludePredicate = edgeIncludePredicate;
    }

    public Function<? super Context<Graph<V,E>,E>,Number> getEdgeLabelClosenessTransformer() {
        return edgeLabelClosenessTransformer;
    }

    public void setEdgeLabelClosenessTransformer(
    		Function<? super Context<Graph<V,E>,E>,Number> edgeLabelClosenessTransformer) {
        this.edgeLabelClosenessTransformer = edgeLabelClosenessTransformer;
    }

    public EdgeLabelRenderer getEdgeLabelRenderer() {
        return edgeLabelRenderer;
    }

    public void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer) {
        this.edgeLabelRenderer = edgeLabelRenderer;
    }

    public Function<? super E,Paint> getEdgeFillPaintTransformer() {
        return edgeFillPaintTransformer;
    }

    public void setEdgeDrawPaintTransformer(Function<? super E,Paint> edgeDrawPaintTransformer) {
        this.edgeDrawPaintTransformer = edgeDrawPaintTransformer;
    }

    public Function<? super E,Paint> getEdgeDrawPaintTransformer() {
        return edgeDrawPaintTransformer;
    }

    public void setEdgeFillPaintTransformer(Function<? super E,Paint> edgeFillPaintTransformer) {
        this.edgeFillPaintTransformer = edgeFillPaintTransformer;
    }

    public Function<? super E, Shape> getEdgeShapeTransformer() {
        return edgeShapeTransformer;
    }

    public void setEdgeShapeTransformer(Function<? super E, Shape> edgeShapeTransformer) {
        this.edgeShapeTransformer = edgeShapeTransformer;
        if (edgeShapeTransformer instanceof ParallelEdgeShapeTransformer) {
        	@SuppressWarnings("unchecked")
			ParallelEdgeShapeTransformer<V, E> transformer =
        			(ParallelEdgeShapeTransformer<V, E>)edgeShapeTransformer;
        	if (transformer instanceof EdgeShape.Orthogonal) {
        		transformer.setEdgeIndexFunction(this.incidentEdgeIndexFunction);
        	} else {
        		transformer.setEdgeIndexFunction(this.parallelEdgeIndexFunction);
        	}
        }
    }

    public Function<? super E,String> getEdgeLabelTransformer() {
        return edgeLabelTransformer;
    }

    public void setEdgeLabelTransformer(Function<? super E,String> edgeLabelTransformer) {
        this.edgeLabelTransformer = edgeLabelTransformer;
    }

    public Function<? super E,Stroke> getEdgeStrokeTransformer() {
        return edgeStrokeTransformer;
    }

    public void setEdgeStrokeTransformer(Function<? super E,Stroke> edgeStrokeTransformer) {
        this.edgeStrokeTransformer = edgeStrokeTransformer;
    }

    public Function<? super E,Stroke> getEdgeArrowStrokeTransformer() {
        return edgeArrowStrokeTransformer;
    }

    public void setEdgeArrowStrokeTransformer(Function<? super E,Stroke> edgeArrowStrokeTransformer) {
        this.edgeArrowStrokeTransformer = edgeArrowStrokeTransformer;
    }

    public GraphicsDecorator getGraphicsContext() {
        return graphicsContext;
    }

    public void setGraphicsContext(GraphicsDecorator graphicsContext) {
        this.graphicsContext = graphicsContext;
    }

    public int getLabelOffset() {
        return labelOffset;
    }

    public void setLabelOffset(int labelOffset) {
        this.labelOffset = labelOffset;
    }

    public EdgeIndexFunction<V, E> getParallelEdgeIndexFunction() {
        return parallelEdgeIndexFunction;
    }

    public void setParallelEdgeIndexFunction(
            EdgeIndexFunction<V, E> parallelEdgeIndexFunction) {
        this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
        // reset the edge shape Function, as the parallel edge index function
        // is used by it
        this.setEdgeShapeTransformer(getEdgeShapeTransformer());
    }

    public PickedState<E> getPickedEdgeState() {
        return pickedEdgeState;
    }

    public void setPickedEdgeState(PickedState<E> pickedEdgeState) {
        this.pickedEdgeState = pickedEdgeState;
    }

    public PickedState<V> getPickedVertexState() {
        return pickedVertexState;
    }

    public void setPickedVertexState(PickedState<V> pickedVertexState) {
        this.pickedVertexState = pickedVertexState;
    }

    public CellRendererPane getRendererPane() {
        return rendererPane;
    }

    public void setRendererPane(CellRendererPane rendererPane) {
        this.rendererPane = rendererPane;
    }

    public JComponent getScreenDevice() {
        return screenDevice;
    }

    public void setScreenDevice(JComponent screenDevice) {
        this.screenDevice = screenDevice;
        screenDevice.add(rendererPane);
    }

    public Function<? super V,Font> getVertexFontTransformer() {
        return vertexFontTransformer;
    }

    public void setVertexFontTransformer(Function<? super V,Font> vertexFontTransformer) {
        this.vertexFontTransformer = vertexFontTransformer;
    }

    public Function<? super V,Icon> getVertexIconTransformer() {
        return vertexIconTransformer;
    }

    public void setVertexIconTransformer(Function<? super V,Icon> vertexIconTransformer) {
        this.vertexIconTransformer = vertexIconTransformer;
    }

    public Predicate<Context<Graph<V,E>,V>> getVertexIncludePredicate() {
        return vertexIncludePredicate;
    }

    public void setVertexIncludePredicate(Predicate<Context<Graph<V,E>,V>> vertexIncludePredicate) {
        this.vertexIncludePredicate = vertexIncludePredicate;
    }

    public VertexLabelRenderer getVertexLabelRenderer() {
        return vertexLabelRenderer;
    }

    public void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer) {
        this.vertexLabelRenderer = vertexLabelRenderer;
    }

    public Function<? super V,Paint> getVertexFillPaintTransformer() {
        return vertexFillPaintTransformer;
    }

    public void setVertexFillPaintTransformer(Function<? super V,Paint> vertexFillPaintTransformer) {
        this.vertexFillPaintTransformer = vertexFillPaintTransformer;
    }

    public Function<? super V,Paint> getVertexDrawPaintTransformer() {
        return vertexDrawPaintTransformer;
    }

    public void setVertexDrawPaintTransformer(Function<? super V,Paint> vertexDrawPaintTransformer) {
        this.vertexDrawPaintTransformer = vertexDrawPaintTransformer;
    }

    public Function<? super V,String> getVertexLabelTransformer() {
        return vertexLabelTransformer;
    }

    public void setVertexLabelTransformer(Function<? super V,String> vertexLabelTransformer) {
        this.vertexLabelTransformer = vertexLabelTransformer;
    }

	public GraphElementAccessor<V, E> getPickSupport() {
		return pickSupport;
	}

	public void setPickSupport(GraphElementAccessor<V, E> pickSupport) {
		this.pickSupport = pickSupport;
	}
	
	public MultiLayerTransformer getMultiLayerTransformer() {
		return multiLayerTransformer;
	}

	public void setMultiLayerTransformer(MultiLayerTransformer basicTransformer) {
		this.multiLayerTransformer = basicTransformer;
	}

	public Function<? super E, Paint> getArrowDrawPaintTransformer() {
		return arrowDrawPaintTransformer;
	}

	public Function<? super E, Paint> getArrowFillPaintTransformer() {
		return arrowFillPaintTransformer;
	}

	public void setArrowDrawPaintTransformer(Function<? super E, Paint> arrowDrawPaintTransformer) {
		this.arrowDrawPaintTransformer = arrowDrawPaintTransformer;
		
	}

	public void setArrowFillPaintTransformer(Function<? super E, Paint> arrowFillPaintTransformer) {
		this.arrowFillPaintTransformer = arrowFillPaintTransformer;
		
	}
}


