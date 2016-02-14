package edu.uci.ics.jung.samples;
/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser;

/**
 * Demonstrates "collapsing"/"expanding" of a tree's subtrees.
 * @author Tom Nelson
 * 
 */
@SuppressWarnings("serial")
public class TreeCollapseDemo extends JApplet {

    /**
     * the graph
     */
    Forest<String,Integer> graph;

    Supplier<DirectedGraph<String,Integer>> graphFactory = 
    	new Supplier<DirectedGraph<String,Integer>>() {

			public DirectedGraph<String, Integer> get() {
				return new DirectedSparseMultigraph<String,Integer>();
			}
		};
			
        Supplier<Tree<String,Integer>> treeFactory =
		new Supplier<Tree<String,Integer>> () {

		public Tree<String, Integer> get() {
			return new DelegateTree<String,Integer>(graphFactory);
		}
	};
	
	
	
	Supplier<Integer> edgeFactory = new Supplier<Integer>() {
		int i=0;
		public Integer get() {
			return i++;
		}};

    Supplier<String> vertexFactory = new Supplier<String>() {
    	int i=0;
		public String get() {
			return "V"+i++;
		}};

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String,Integer> vv;

    VisualizationServer.Paintable rings;

    String root;

    TreeLayout<String,Integer> layout;
	FRLayout<?, ?> layout1;

    TreeCollapser collapser;

    RadialTreeLayout<String,Integer> radialLayout;

	public TreeCollapseDemo() {

        // create a simple graph for the demo
        graph = new DelegateForest<String,Integer>();

        createTree();

        layout = new TreeLayout<String,Integer>(graph);
        collapser = new TreeCollapser();

        radialLayout = new RadialTreeLayout<String,Integer>(graph);
        radialLayout.setSize(new Dimension(600,600));
        vv =  new VisualizationViewer<String,Integer>(layout, new Dimension(600,600));
        vv.setBackground(Color.white);
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeFunction<String>());
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        vv.getRenderContext().setArrowFillPaintTransformer(Functions.<Paint>constant(Color.lightGray));
        rings = new Rings();

        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);

        final DefaultModalGraphMouse<String, Integer> graphMouse
        	= new DefaultModalGraphMouse<String, Integer>();

        vv.setGraphMouse(graphMouse);

        JComboBox<?> modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });

        JToggleButton radial = new JToggleButton("Radial");
        radial.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					vv.setGraphLayout(radialLayout);
					vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
					vv.addPreRenderPaintable(rings);
				} else {
					vv.setGraphLayout(layout);
					vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
					vv.removePreRenderPaintable(rings);
				}
				vv.repaint();
			}});

        JButton collapse = new JButton("Collapse");
        collapse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Collection<String> picked
                	= new HashSet<String>(vv.getPickedVertexState().getPicked());
                if(picked.size() == 1) {
                	Object root = picked.iterator().next();
                    Forest<String, Integer> inGraph = (Forest<String, Integer>)layout.getGraph();

                    try {
						collapser.collapse(vv.getGraphLayout(), inGraph, root);
					} catch (InstantiationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

                    vv.getPickedVertexState().clear();
                    vv.repaint();
                }
            }});

        JButton expand = new JButton("Expand");
        expand.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Collection<String> picked = vv.getPickedVertexState().getPicked();
                for(Object v : picked) {
                    if(v instanceof Forest) {
                        Forest<String, Integer> inGraph
                        	= (Forest<String, Integer>)layout.getGraph();
            			collapser.expand(inGraph, (Forest<?, ?>)v);
                    }
                    vv.getPickedVertexState().clear();
                   vv.repaint();
                }
            }});

        JPanel scaleGrid = new JPanel(new GridLayout(1,0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        controls.add(radial);
        controls.add(scaleGrid);
        controls.add(modeBox);
        controls.add(collapse);
        controls.add(expand);
        content.add(controls, BorderLayout.SOUTH);
    }

    class Rings implements VisualizationServer.Paintable {
    	
    	Collection<Double> depths;
    	
    	public Rings() {
    		depths = getDepths();
    	}
    	
    	private Collection<Double> getDepths() {
    		Set<Double> depths = new HashSet<Double>();
    		Map<String,PolarPoint> polarLocations = radialLayout.getPolarLocations();
    		for(String v : graph.getVertices()) {
    			PolarPoint pp = polarLocations.get(v);
    			depths.add(pp.getRadius());
    		}
    		return depths;
    	}

		public void paint(Graphics g) {
			g.setColor(Color.lightGray);
		
			Graphics2D g2d = (Graphics2D)g;
			Point2D center = radialLayout.getCenter();

			Ellipse2D ellipse = new Ellipse2D.Double();
			for(double d : depths) {
				ellipse.setFrameFromDiagonal(center.getX()-d, center.getY()-d, 
						center.getX()+d, center.getY()+d);
				Shape shape = vv.getRenderContext().
						getMultiLayerTransformer().getTransformer(Layer.LAYOUT).transform(ellipse);
				g2d.draw(shape);
			}
		}

		public boolean useTransform() {
			return true;
		}
    }

    /**
     * 
     */
    private void createTree() {
    	graph.addVertex("V0");
    	graph.addEdge(edgeFactory.get(), "V0", "V1");
    	graph.addEdge(edgeFactory.get(), "V0", "V2");
    	graph.addEdge(edgeFactory.get(), "V1", "V4");
    	graph.addEdge(edgeFactory.get(), "V2", "V3");
    	graph.addEdge(edgeFactory.get(), "V2", "V5");
    	graph.addEdge(edgeFactory.get(), "V4", "V6");
    	graph.addEdge(edgeFactory.get(), "V4", "V7");
    	graph.addEdge(edgeFactory.get(), "V3", "V8");
    	graph.addEdge(edgeFactory.get(), "V6", "V9");
    	graph.addEdge(edgeFactory.get(), "V4", "V10");
    	
       	graph.addVertex("A0");
       	graph.addEdge(edgeFactory.get(), "A0", "A1");
       	graph.addEdge(edgeFactory.get(), "A0", "A2");
       	graph.addEdge(edgeFactory.get(), "A0", "A3");
       	
       	graph.addVertex("B0");
    	graph.addEdge(edgeFactory.get(), "B0", "B1");
    	graph.addEdge(edgeFactory.get(), "B0", "B2");
    	graph.addEdge(edgeFactory.get(), "B1", "B4");
    	graph.addEdge(edgeFactory.get(), "B2", "B3");
    	graph.addEdge(edgeFactory.get(), "B2", "B5");
    	graph.addEdge(edgeFactory.get(), "B4", "B6");
    	graph.addEdge(edgeFactory.get(), "B4", "B7");
    	graph.addEdge(edgeFactory.get(), "B3", "B8");
    	graph.addEdge(edgeFactory.get(), "B6", "B9");
       	
    }

        /**
     * a demo class that will create a vertex shape that is either a
     * polygon or star. The number of sides corresponds to the number
     * of vertices that were collapsed into the vertex represented by
     * this shape.
     * 
     * @author Tom Nelson
     *
     * @param <V> the vertex type
     */
    class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V>
{

        ClusterVertexShapeFunction() {
            setSizeTransformer(new ClusterVertexSizeFunction<V>(20));
        }
		@Override
        public Shape apply(V v) {
            if(v instanceof Graph) {
                @SuppressWarnings("rawtypes")
				int size = ((Graph)v).getVertexCount();
                if (size < 8) {   
                    int sides = Math.max(size, 3);
                    return factory.getRegularPolygon(v, sides);
                }
                else {
                    return factory.getRegularStar(v, size);
                }
            }
            return super.apply(v);
        }
    }

    /**
     * A demo class that will make vertices larger if they represent
     * a collapsed collection of original vertices
     * @author Tom Nelson
     *
     * @param <V> the vertex type
     */
    class ClusterVertexSizeFunction<V> implements Function<V,Integer> {
    	int size;
        public ClusterVertexSizeFunction(Integer size) {
            this.size = size;
        }

        public Integer apply(V v) {
            if(v instanceof Graph) {
                return 30;
            }
            return size;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        content.add(new TreeCollapseDemo());
        frame.pack();
        frame.setVisible(true);
    }
}
