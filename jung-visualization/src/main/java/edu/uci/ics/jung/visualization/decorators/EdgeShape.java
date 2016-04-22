/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.util.ArrowFactory;


/**
 * An interface for decorators that return a 
 * <code>Shape</code> for a specified edge.
 * 
 * All edge shapes must be defined so that their endpoints are at
 * (0,0) and (1,0). They will be scaled, rotated and translated into
 * position by the PluggableRenderer.
 *  
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class EdgeShape<V,E> {
    private static final Line2D LINE = new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
    private static final GeneralPath BENT_LINE = new GeneralPath();
    private static final QuadCurve2D QUAD_CURVE = new QuadCurve2D.Float();
    private static final CubicCurve2D CUBIC_CURVE = new CubicCurve2D.Float();
    private static final Ellipse2D ELLIPSE = new Ellipse2D.Float(-.5f, -.5f, 1, 1);
    private static Rectangle2D BOX = new Rectangle2D.Float();

    private static GeneralPath triangle;
    private static GeneralPath bowtie;

	protected final Graph<V, E> graph;
	
    /**
     * A convenience instance for other edge shapes to use for self-loop edges 
     * where parallel instances will not overlay each other.
     */
    protected final Loop loop;
    
    /**
     * A convenience instance for other edge shapes to use for self-loop edges
     * where parallel instances overlay each other.
     */
    protected final SimpleLoop simpleLoop;

    protected final Box box;

	public EdgeShape(Graph<V, E> g) {
		this.graph = g;
		this.box = new Box();
		this.loop = new Loop();
		this.simpleLoop = new SimpleLoop();
	}
	
	private Shape getLoopOrNull(E e) {
		return getLoopOrNull(e, loop);
	}
	
	private Shape getLoopOrNull(E e, Function<? super E, Shape> loop) {
        Pair<V> endpoints = graph.getEndpoints(e);
        checkNotNull(endpoints);
    	boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
    	if (isLoop) {
    		return loop.apply(e);
    	}
        return null;
	}
	
	public static <V, E> EdgeShape<V, E>.Line line(Graph<V, E> graph) {
        return new EdgeShape<V, E>(graph).new Line();
	}
	
	public static <V, E> EdgeShape<V, E>.QuadCurve quadCurve(Graph<V, E> graph) {
        return new EdgeShape<V, E>(graph).new QuadCurve();
	}
	
	public static <V, E> EdgeShape<V, E>.QuadCurve cubicCurve(Graph<V, E> graph) {
        return new EdgeShape<V, E>(graph).new QuadCurve();
	}
	
	public static <V, E> EdgeShape<V, E>.Orthogonal orthogonal(Graph<V, E> graph) {
		return new EdgeShape<V, E>(graph).new Orthogonal();
	}
	
	public static <V, E> EdgeShape<V, E>.Wedge wedge(Graph<V, E> graph, int width) {
		return new EdgeShape<V, E>(graph).new Wedge(width);
	}
	
    /**
     * An edge shape that renders as a straight line between
     * the vertex endpoints.
     */
    public class Line implements Function<E, Shape> {
        /**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the 
         * Loop shared instance.
         */
		public Shape apply(E e) {
			Shape loop = getLoopOrNull(e);
			return loop == null
					? LINE
					: loop;
        }
    }

    private int getIndex(E e, EdgeIndexFunction<V, E> edgeIndexFunction) {
    	return edgeIndexFunction == null
    			? 1
    			: edgeIndexFunction.getIndex(graph, e);
    }
    
    /**
     * An edge shape that renders as a bent-line between the
     * vertex endpoints.
     */
    public class BentLine extends ParallelEdgeShapeTransformer<V,E> {
		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> edgeIndexFunction) {
			this.edgeIndexFunction = edgeIndexFunction;
            loop.setEdgeIndexFunction(edgeIndexFunction);
        }

		/**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
		public Shape apply(E e) {
        	Shape edgeShape = getLoopOrNull(e);
        	if (edgeShape != null) {
        		return edgeShape;
        	}

        	int index = getIndex(e, edgeIndexFunction);
            float controlY = control_offset_increment + control_offset_increment * index;
            BENT_LINE.reset();
            BENT_LINE.moveTo(0.0f, 0.0f);
            BENT_LINE.lineTo(0.5f, controlY);
            BENT_LINE.lineTo(1.0f, 1.0f);
            return BENT_LINE;
        }

    }
    
    /**
     * An edge shape that renders as a QuadCurve between vertex
     * endpoints.
     */
    public class QuadCurve extends ParallelEdgeShapeTransformer<V,E> {
    	@Override
		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.edgeIndexFunction = parallelEdgeIndexFunction;
            loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
        }

    	/**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
		public Shape apply(E e) {
        	Shape edgeShape = getLoopOrNull(e);
        	if (edgeShape != null) {
        		return edgeShape;
        	}
            
            int index = getIndex(e, edgeIndexFunction);
            
            float controlY = control_offset_increment + 
                control_offset_increment * index;
            QUAD_CURVE.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
            return QUAD_CURVE;
        }
    }
    
    /**
     * An edge shape that renders as a CubicCurve between vertex
     * endpoints.  The two control points are at 
     * (1/3*length, 2*controlY) and (2/3*length, controlY)
     * giving a 'spiral' effect.
     */
    public class CubicCurve extends ParallelEdgeShapeTransformer<V,E> {
		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> edgeIndexFunction) {
            this.edgeIndexFunction = edgeIndexFunction;
            loop.setEdgeIndexFunction(edgeIndexFunction);
       }

		/**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
		public Shape apply(E e) {
        	Shape edgeShape = getLoopOrNull(e);
        	if (edgeShape != null) {
        		return edgeShape;
        	}
            
            int index = getIndex(e, edgeIndexFunction);

			float controlY = control_offset_increment
			    + control_offset_increment * index;
			CUBIC_CURVE.setCurve(0.0f, 0.0f, 0.33f, 2 * controlY, .66f, -controlY,
					1.0f, 0.0f);
            return CUBIC_CURVE;
        }
    }
    
    /**
	 * An edge shape that renders as a loop with its nadir at the center of the
	 * vertex. Parallel instances will overlap.
	 * 
     * @author Tom Nelson 
     */
    public class SimpleLoop extends ParallelEdgeShapeTransformer<V,E> {
        public Shape apply(E e) {
            return ELLIPSE;
        }
    }
    
    private Shape buildFrame(RectangularShape shape, int index) {
        float x = -.5f;
        float y = -.5f;
        float diam = 1.f;
        diam += diam * index/2;
        x += x * index/2;
        y += y * index/2;
    	
        shape.setFrame(x, y, diam, diam);
        
        return shape;
    }
    
    /**
     * An edge shape that renders as a loop with its nadir at the
     * center of the vertex. Parallel instances will not overlap.
     */
    public class Loop extends ParallelEdgeShapeTransformer<V,E> {
        public Shape apply(E e) {
            return buildFrame(ELLIPSE, getIndex(e, edgeIndexFunction));
        }
    }

    /**
     * An edge shape that renders as an isosceles triangle whose
     * apex is at the destination vertex for directed edges,
     * and as a "bowtie" shape for undirected edges.
     * @author Joshua O'Madadhain
     */
    public class Wedge extends ParallelEdgeShapeTransformer<V,E> {
        public Wedge(int width)  {
            triangle = ArrowFactory.getWedgeArrow(width, 1);
            triangle.transform(AffineTransform.getTranslateInstance(1,0));
            bowtie = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            bowtie.moveTo(0, width/2);
            bowtie.lineTo(1, -width/2);
            bowtie.lineTo(1, width/2);
            bowtie.lineTo(0, -width/2);
            bowtie.closePath();
        }
        
        public Shape apply(E e) {
        	Shape edgeShape = getLoopOrNull(e);
        	if (edgeShape != null) {
        		return edgeShape;
        	}
        	return (graph.getEdgeType(e) == EdgeType.DIRECTED)
        			? triangle
        			: bowtie;
        }
    }
    
    /**
     * An edge shape that renders as a diamond with its nadir at the
     * center of the vertex. Parallel instances will not overlap.
     */
    public class Box extends ParallelEdgeShapeTransformer<V,E> {
        public Shape apply(E e) {
            return buildFrame(BOX, getIndex(e, edgeIndexFunction));
        }
    }


    /**
     * An edge shape that renders as a bent-line between the vertex endpoints.
     */
    public class Orthogonal extends ParallelEdgeShapeTransformer<V,E> {
		public Shape apply(E e) {
			Shape loop = getLoopOrNull(e, box);
			return loop == null
					? LINE
					: loop;
        }
    }
}
    

