/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package edu.uci.ics.jung.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Demonstrates the AggregateLayout
 * class. In this demo, vertices are visually clustered as they
 * are selected. The cluster is formed in a new Layout centered at the
 * middle locations of the selected vertices. The size and layout
 * algorithm for each new cluster is selectable.
 * 
 * @author Tom Nelson
 * 
 */
@SuppressWarnings("serial")
public class SubLayoutDemo extends JApplet {

    String instructions =
        "<html>"+
        "Use the Layout combobox to select the "+
        "<p>underlying layout."+
        "<p>Use the SubLayout combobox to select "+
        "<p>the type of layout for any clusters you create."+
        "<p>To create clusters, use the mouse to select "+
        "<p>multiple vertices, either by dragging a region, "+
        "<p>or by shift-clicking on multiple vertices."+
        "<p>After you select vertices, use the "+
        "<p>Cluster Picked button to cluster them using the "+
        "<p>layout and size specified in the Sublayout comboboxen."+
        "<p>Use the Uncluster All button to remove all"+
        "<p>clusters."+
        "<p>You can drag the cluster with the mouse." +
        "<p>Use the 'Picking'/'Transforming' combo-box to switch"+
        "<p>between picking and transforming mode.</html>";
    /**
     * the graph
     */
    Graph<String,Number> graph;
    
    Map<Graph<String,Number>,Dimension> sizes = new HashMap<Graph<String,Number>,Dimension>();

    Class[] layoutClasses = new Class[]{CircleLayout.class,SpringLayout.class,FRLayout.class,KKLayout.class};
    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String,Number> vv;

    AggregateLayout<String,Number> clusteringLayout;
    
    Dimension subLayoutSize;
    
    PickedState<String> ps;
    
    Class subLayoutType = CircleLayout.class;
    
    /**
     * create an instance of a simple graph with controls to
     * demo the zoomand hyperbolic features.
     * 
     */
    public SubLayoutDemo() {
        
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph();

        // ClusteringLayout is a decorator class that delegates
        // to another layout, but can also sepately manage the
        // layout of sub-sets of vertices in circular clusters.
        clusteringLayout = new AggregateLayout<String,Number>(new FRLayout<String,Number>(graph));
        	//new SubLayoutDecorator<String,Number>(new FRLayout<String,Number>(graph));

        Dimension preferredSize = new Dimension(600,600);
        final VisualizationModel<String,Number> visualizationModel = 
            new DefaultVisualizationModel<String,Number>(clusteringLayout, preferredSize);
        vv =  new VisualizationViewer<String,Number>(visualizationModel, preferredSize);
        
        ps = vv.getPickedVertexState();
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.red));
        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(vv.getPickedVertexState(), 
                Color.red, Color.yellow));
        vv.setBackground(Color.white);
        
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        
        /**
         * the regular graph mouse for the normal view
         */
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

        vv.setGraphMouse(graphMouse);
        
        Container content = getContentPane();
        GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
        content.add(gzsp);
        
        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        
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
        
        JButton cluster = new JButton("Cluster Picked");
        cluster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clusterPicked();
			}});
        
        JButton uncluster = new JButton("UnCluster All");
        uncluster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uncluster();
			}});
        
        JComboBox layoutTypeComboBox = new JComboBox(layoutClasses);
        layoutTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        layoutTypeComboBox.setSelectedItem(FRLayout.class);
        layoutTypeComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					Class clazz = (Class)e.getItem();
					try {
						Layout<String,Number> layout = getLayoutFor(clazz, graph);
						layout.setInitializer(vv.getGraphLayout());
						clusteringLayout.setDelegate(layout);
						vv.setGraphLayout(clusteringLayout);
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}});
        
        JComboBox subLayoutTypeComboBox = new JComboBox(layoutClasses);
        
        subLayoutTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        subLayoutTypeComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					subLayoutType = (Class)e.getItem();
				}
			}});
        
        JComboBox subLayoutDimensionComboBox = 
        	new JComboBox(new Dimension[]{
        			new Dimension(75,75),
        			new Dimension(100,100),
        			new Dimension(150,150),
        			new Dimension(200,200),
        			new Dimension(250,250),
        			new Dimension(300,300)
        	}	
        	);
        subLayoutDimensionComboBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('['));
                valueString = valueString.replaceAll("idth", "");
                valueString = valueString.replaceAll("eight","");
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        subLayoutDimensionComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					subLayoutSize = (Dimension)e.getItem();
				}
			}});
        subLayoutDimensionComboBox.setSelectedIndex(1);

        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog((JComponent)e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE);
            }
        });
        Dimension space = new Dimension(20,20);
        Box controls = Box.createVerticalBox();
        controls.add(Box.createRigidArea(space));
        
        JPanel zoomControls = new JPanel(new GridLayout(1,2));
        zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomControls.add(plus);
        zoomControls.add(minus);
        heightConstrain(zoomControls);
        controls.add(zoomControls);
        controls.add(Box.createRigidArea(space));
        
        JPanel clusterControls = new JPanel(new GridLayout(0,1));
        clusterControls.setBorder(BorderFactory.createTitledBorder("Clustering"));
        clusterControls.add(cluster);
        clusterControls.add(uncluster);
        heightConstrain(clusterControls);
        controls.add(clusterControls);
        controls.add(Box.createRigidArea(space));
        
        JPanel layoutControls = new JPanel(new GridLayout(0,1));
        layoutControls.setBorder(BorderFactory.createTitledBorder("Layout"));
        layoutControls.add(layoutTypeComboBox);
        heightConstrain(layoutControls);
        controls.add(layoutControls);

        JPanel subLayoutControls = new JPanel(new GridLayout(0,1));
        subLayoutControls.setBorder(BorderFactory.createTitledBorder("SubLayout"));
        subLayoutControls.add(subLayoutTypeComboBox);
        subLayoutControls.add(subLayoutDimensionComboBox);
        heightConstrain(subLayoutControls);
        controls.add(subLayoutControls);
        controls.add(Box.createRigidArea(space));
        
        JPanel modePanel = new JPanel(new GridLayout(1,1));
        modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        modePanel.add(modeBox);
        heightConstrain(modePanel);
        controls.add(modePanel);
        controls.add(Box.createRigidArea(space));

        controls.add(help);
        controls.add(Box.createVerticalGlue());
        content.add(controls, BorderLayout.EAST);
    }
    
    private void heightConstrain(Component component) {
    	Dimension d = new Dimension(component.getMaximumSize().width,
    			component.getMinimumSize().height);
    	component.setMaximumSize(d);
    }
    
    private Layout getLayoutFor(Class layoutClass, Graph graph) throws Exception {
    	Object[] args = new Object[]{graph};
    	Constructor constructor = layoutClass.getConstructor(new Class[] {Graph.class});
    	return  (Layout)constructor.newInstance(args);
    }
    
    private void clusterPicked() {
    	cluster(true);
    }
    
    private void uncluster() {
    	cluster(false);
    }

    private void cluster(boolean state) {
    	if(state == true) {
    		// put the picked vertices into a new sublayout 
    		Collection<String> picked = ps.getPicked();
    		if(picked.size() > 1) {
    			Point2D center = new Point2D.Double();
    			double x = 0;
    			double y = 0;
    			for(String vertex : picked) {
    				Point2D p = clusteringLayout.transform(vertex);
    				x += p.getX();
    				y += p.getY();
    			}
    			x /= picked.size();
    			y /= picked.size();
				center.setLocation(x,y);

//    			String firstVertex = picked.iterator().next();
//    			Point2D center = clusteringLayout.transform(firstVertex);
    			Graph<String, Number> subGraph;
    			try {
    				subGraph = graph.getClass().newInstance();
    				for(String vertex : picked) {
    					subGraph.addVertex(vertex);
    					Collection<Number> incidentEdges = graph.getIncidentEdges(vertex);
    					for(Number edge : incidentEdges) {
    						Pair<String> endpoints = graph.getEndpoints(edge);
    						if(picked.containsAll(endpoints)) {
    							// put this edge into the subgraph
    							subGraph.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
    						}
    					}
    				}

    				Layout<String,Number> subLayout = getLayoutFor(subLayoutType, subGraph);
    				subLayout.setInitializer(vv.getGraphLayout());
    				subLayout.setSize(subLayoutSize);
    				clusteringLayout.put(subLayout,center);
    				vv.setGraphLayout(clusteringLayout);

    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	} else {
    		// remove all sublayouts
    		this.clusteringLayout.removeAll();
    		vv.setGraphLayout(clusteringLayout);
    	}
    }

    /**
     * a driver for this demo
     */
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new SubLayoutDemo());
        f.pack();
        f.setVisible(true);
    }
}
