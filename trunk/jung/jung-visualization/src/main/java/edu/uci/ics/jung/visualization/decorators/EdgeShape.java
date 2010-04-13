/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
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
 * @param <Edge>
 */
public class EdgeShape<V,E>  {
    
    /**
     * a convenience instance for other edge shapes to use
     * for self-loop edges where parallel instances will not
     * overlay each other.
     */
    @SuppressWarnings("unchecked")
    protected static Loop loop = new Loop();
    
    /**
     * a convenience instance for other edge shapes to use
     * for self-loop edges where parallel instances overlay each
     * other
     */
    @SuppressWarnings("unchecked")
    protected static SimpleLoop simpleLoop = new SimpleLoop();
    
    @SuppressWarnings("unchecked")
    protected static Box box = new Box();

    /**
     * An edge shape that renders as a straight line between
     * the vertex endpoints.
     */
    public static class Line<V,E> extends AbstractEdgeShapeTransformer<V,E> {

        /**
         * Singleton instance of the Line2D edge shape
         */
        private static Line2D instance = new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
        /**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * SimpleLoop shared instance.
         */
        @SuppressWarnings("unchecked")
		public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
            
            Pair<V> endpoints = graph.getEndpoints(e);
            if(endpoints != null) {
            	boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
            	if (isLoop) {
            		return loop.transform(context);
            	}
            }
            return instance;
        }
    }
    
    /**
     * An edge shape that renders as a bent-line between the
     * vertex endpoints.
     */
    public static class BentLine<V,E> 
             extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E> {
        
        /**
         * singleton instance of the BentLine shape
         */
        private static GeneralPath instance = new GeneralPath();
        
        protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction;

        @SuppressWarnings("unchecked")
		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
            loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
        }
        
        

        /**
		 * @return the parallelEdgeIndexFunction
		 */
		public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
			return parallelEdgeIndexFunction;
		}



		/**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
        @SuppressWarnings("unchecked")
		public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
            Pair<V> endpoints = graph.getEndpoints(e);
            if(endpoints != null) {
            	boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
            	if (isLoop) {
            		return loop.transform(context);
            	}
            }
            
            int index = 1;
            if(parallelEdgeIndexFunction != null) {
                index = parallelEdgeIndexFunction.getIndex(graph, e);
            }
            float controlY = control_offset_increment + control_offset_increment*index;
            instance.reset();
            instance.moveTo(0.0f, 0.0f);
            instance.lineTo(0.5f, controlY);
            instance.lineTo(1.0f, 1.0f);
            return instance;
        }

    }
    
    /**
     * An edge shape that renders as a QuadCurve between vertex
     * endpoints.
     */
    public static class QuadCurve<V,E>
           extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E> {
        
        /**
         * singleton instance of the QuadCurve shape
         */
        private static QuadCurve2D instance = new QuadCurve2D.Float();
        
        protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction;

        @SuppressWarnings("unchecked")
		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
            loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
        }

       /**
		 * @return the parallelEdgeIndexFunction
		 */
		public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
			return parallelEdgeIndexFunction;
		}

	/**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
        @SuppressWarnings("unchecked")
		public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
            Pair<V> endpoints = graph.getEndpoints(e);
            if(endpoints != null) {
            	boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
            	if (isLoop) {
            		return loop.transform(context);
            	}
            }
            
            int index = 1;
            if(parallelEdgeIndexFunction != null) {
                index = parallelEdgeIndexFunction.getIndex(graph, e);
            }
            
            float controlY = control_offset_increment + 
                control_offset_increment * index;
            instance.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
            return instance;
        }
    }
    
    /**
     * An edge shape that renders as a CubicCurve between vertex
     * endpoints.  The two control points are at 
     * (1/3*length, 2*controlY) and (2/3*length, controlY)
     * giving a 'spiral' effect.
     */
    public static class CubicCurve<V,E> 
         extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E> {
        
        /**
         * singleton instance of the CubicCurve edge shape
         */
        private static CubicCurve2D instance = new CubicCurve2D.Float();
        
        protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction;

        @SuppressWarnings("unchecked")
		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
            loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
       }

        /**
		 * @return the parallelEdgeIndexFunction
		 */
		public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
			return parallelEdgeIndexFunction;
		}

		/**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
        @SuppressWarnings("unchecked")
		public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
           Pair<V> endpoints = graph.getEndpoints(e);
           if(endpoints != null) {
        	   boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
        	   if (isLoop) {
        		   return loop.transform(context);
        	   }
           }
           
           int index = 1;
           if(parallelEdgeIndexFunction != null) {
               index = parallelEdgeIndexFunction.getIndex(graph, e);
           }

			float controlY = control_offset_increment
			    + control_offset_increment * index;
			instance.setCurve(0.0f, 0.0f, 0.33f, 2 * controlY, .66f, -controlY,
					1.0f, 0.0f);
            return instance;
        }
    }
    
    /**
	 * An edge shape that renders as a loop with its nadir at the center of the
	 * vertex. Parallel instances will overlap.
	 * 
     * @author Tom Nelson 
     */
    public static class SimpleLoop<V,E> extends AbstractEdgeShapeTransformer<V,E> {
        
        /**
         * singleton instance of the SimpleLoop shape
         */
        private static Ellipse2D instance = new Ellipse2D.Float(-.5f, -.5f, 1, 1);
        
        /**
         * getter for the shape
         * @return the shared instance
         */
        public Shape transform(Context<Graph<V,E>,E> context) {
            return instance;
        }
    }
    
    /**
     * An edge shape that renders as a loop with its nadir at the
     * center of the vertex. Parallel instances will not overlap.
     */
    public static class Loop<V,E>
           extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E> {
        
        /**
         * singleton instance of the Loop shape
         */
        private static Ellipse2D instance = new Ellipse2D.Float();
        
        protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction;

        public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
        }


        /**
		 * @return the parallelEdgeIndexFunction
		 */
		public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
			return parallelEdgeIndexFunction;
		}


		/**
         * Get the shape for this edge, modifying the diameter in the
         * case of parallel edges, so they do not overlap
         */
        public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
            int count = 1;
            if(parallelEdgeIndexFunction != null) {
                count = parallelEdgeIndexFunction.getIndex(graph, e);
            }
            
            float x = -.5f;
            float y = -.5f;
            float diam = 1.f;
            diam += diam*count/2;
            x += x*count/2;
            y += y*count/2;
            instance.setFrame(x,y,diam,diam);
            return instance;
        }
    }

    /**
     * An edge shape that renders as an isosceles triangle whose
     * apex is at the destination vertex for directed edges,
     * and as a "bowtie" shape for undirected edges.
     * @author Joshua O'Madadhain
     */
    public static class Wedge<V,E> extends AbstractEdgeShapeTransformer<V,E> {
        private static GeneralPath triangle;
        private static GeneralPath bowtie;
        
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
        
        public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
        
            Pair<V> endpoints = graph.getEndpoints(e);
            if(endpoints != null) {
            	boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
            	if (isLoop) {
            		return Loop.instance;
            	}
            }
            if (graph.getEdgeType(e) == EdgeType.DIRECTED)
                return triangle;
            else
                return bowtie;
        }
    }
    
    /**
     * An edge shape that renders as a loop with its nadir at the
     * center of the vertex. Parallel instances will not overlap.
     */
    public static class Box<V,E>
           extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E> {
        
        /**
         * singleton instance of the Loop shape
         */
        private static Rectangle2D instance = new Rectangle2D.Float();
        
        protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction;

        public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
        }

        /**
		 * @return the parallelEdgeIndexFunction
		 */
		public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
			return parallelEdgeIndexFunction;
		}

		/**
         * Get the shape for this edge, modifying the diameter in the
         * case of parallel edges, so they do not overlap
         */
        public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
            int count = 1;
            if(parallelEdgeIndexFunction != null) {
                count = parallelEdgeIndexFunction.getIndex(graph, e);
            }
            
            float x = -.5f;
            float y = -.5f;
            float diam = 1.f;
            diam += diam*count/2;
            x += x*count/2;
            y += y*count/2;
            instance.setFrame(x,y,diam,diam);
            return instance;
        }
    }


    /**
     * An edge shape that renders as a bent-line between the
     * vertex endpoints.
     */
    public static class Orthogonal<V,E> 
             extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E> {
        
        /**
         * singleton instance of the BentLine shape
         */
        private static Line2D instance = new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
        
        protected EdgeIndexFunction<V,E> edgeIndexFunction;

        @SuppressWarnings("unchecked")
		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> edgeIndexFunction) {
            this.edgeIndexFunction = edgeIndexFunction;
            box.setEdgeIndexFunction(edgeIndexFunction);
        }

        /**
		 * @return the parallelEdgeIndexFunction
		 */
		public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
			return edgeIndexFunction;
		}

		/**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
        @SuppressWarnings("unchecked")
		public Shape transform(Context<Graph<V,E>,E> context) {
        	Graph<V,E> graph = context.graph;
        	E e = context.element;
            Pair<V> endpoints = graph.getEndpoints(e);
            if(endpoints != null) {
            	boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
            	if (isLoop) {
            		return box.transform(context);
            	}
            }
            return instance;
        }
    }
    
    public static interface IndexedRendering<V,E> {
        void setEdgeIndexFunction(EdgeIndexFunction<V,E> peif);
        EdgeIndexFunction<V,E> getEdgeIndexFunction();
    }
}
    

