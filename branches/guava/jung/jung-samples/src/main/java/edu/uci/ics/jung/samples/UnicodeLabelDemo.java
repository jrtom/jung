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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;

/**
 * A demo that shows flag images as vertices, and uses unicode
 * to render vertex labels.
 * 
 * @author Tom Nelson 
 * 
 */
public class UnicodeLabelDemo {

    /**
     * the graph
     */
    Graph<Integer,Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Integer,Number> vv;
    
    boolean showLabels;
    
    public UnicodeLabelDemo() {
        
        // create a simple graph for the demo
        graph = new DirectedSparseGraph<Integer,Number>();
        Integer[] v = createVertices(10);
        createEdges(v);
        
        vv =  new VisualizationViewer<Integer,Number>(new FRLayout<Integer,Number>(graph));
        vv.getRenderContext().setVertexLabelTransformer(new UnicodeVertexStringer<Integer>(v));
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
        VertexIconShapeTransformer<Integer> vertexIconShapeFunction =
            new VertexIconShapeTransformer<Integer>(new EllipseVertexShapeTransformer<Integer>());
        DefaultVertexIconTransformer<Integer> vertexIconFunction = new DefaultVertexIconTransformer<Integer>();
        vv.getRenderContext().setVertexShapeTransformer(vertexIconShapeFunction);
        vv.getRenderContext().setVertexIconTransformer(vertexIconFunction);
        loadImages(v, vertexIconFunction.getIconMap());
        vertexIconShapeFunction.setIconMap(vertexIconFunction.getIconMap());
        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.white,  Color.yellow));
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.lightGray));

        vv.setBackground(Color.white);

        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Integer>());
        
        // create a frome to hold the graph
        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer,Number>();
        vv.setGraphMouse(gm);
        
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

        JCheckBox lo = new JCheckBox("Show Labels");
        lo.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                showLabels = e.getStateChange() == ItemEvent.SELECTED;
                vv.repaint();
            }
        });
        lo.setSelected(true);
        
        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        controls.add(lo);
        controls.add(((DefaultModalGraphMouse<Integer,Number>) gm).getModeComboBox());
        content.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
    
    
    class UnicodeVertexStringer<V> implements Transformer<V,String> {

        Map<V,String> map = new HashMap<V,String>();
        Map<V,Icon> iconMap = new HashMap<V,Icon>();
        String[] labels = {
                "\u0057\u0065\u006C\u0063\u006F\u006D\u0065\u0020\u0074\u006F\u0020JUNG\u0021",               
                "\u6B22\u8FCE\u4F7F\u7528\u0020\u0020JUNG\u0021",
                "\u0414\u043E\u0431\u0440\u043E\u0020\u043F\u043E\u0436\u0430\u043B\u043E\u0432\u0430\u0422\u044A\u0020\u0432\u0020JUNG\u0021",
                "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0075\u0065\u0020\u0061\u0075\u0020JUNG\u0021",
                "\u0057\u0069\u006C\u006B\u006F\u006D\u006D\u0065\u006E\u0020\u007A\u0075\u0020JUNG\u0021",
                "JUNG\u3078\u3087\u3045\u3053\u305D\u0021",
//                "\u0053\u00E9\u006A\u0061\u0020\u0042\u0065\u006D\u0076\u0069\u006E\u0064\u006F\u0020JUNG\u0021",
               "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0069\u0064\u0061\u0020\u0061\u0020JUNG\u0021"
        };
        
        public UnicodeVertexStringer(V[] vertices) {
            for(int i=0; i<vertices.length; i++) {
                map.put(vertices[i], labels[i%labels.length]);
            }
        }
        
        /**
         * @see edu.uci.ics.jung.graph.decorators.VertexStringer#getLabel(edu.uci.ics.jung.graph.Vertex)
         */
        public String getLabel(V v) {
            if(showLabels) {
                return (String)map.get(v);
            } else {
                return "";
            }
        }
        
		public String transform(V input) {
			return getLabel(input);
		}
    }
    
    /**
     * create some vertices
     * @param count how many to create
     * @return the Vertices in an array
     */
    private Integer[] createVertices(int count) {
        Integer[] v = new Integer[count];
        for (int i = 0; i < count; i++) {
            v[i] = new Integer(i);
            graph.addVertex(v[i]);
        }
        return v;
    }

    /**
     * create edges for this demo graph
     * @param v an array of Vertices to connect
     */
    void createEdges(Integer[] v) {
        graph.addEdge(new Double(Math.random()), v[0], v[1], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[3], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[4], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[3], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[4], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[8], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[3], v[8], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[6], v[7], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[7], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[9], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[9], v[8], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[7], v[6], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[6], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[5], v[4], EdgeType.DIRECTED);
    }

    /**
     * A nested class to demo ToolTips
     */
    
    protected void loadImages(Integer[] vertices, Map<Integer,Icon> imageMap) {
        
        ImageIcon[] icons = null;
        try {
            icons = new ImageIcon[] {
                    new ImageIcon(getClass().getResource("/images/united-states.gif")),
                    new ImageIcon(getClass().getResource("/images/china.gif")),
                    new ImageIcon(getClass().getResource("/images/russia.gif")),
                    new ImageIcon(getClass().getResource("/images/france.gif")),
                    new ImageIcon(getClass().getResource("/images/germany.gif")),
                    new ImageIcon(getClass().getResource("/images/japan.gif")),
                    new ImageIcon(getClass().getResource("/images/spain.gif"))
            };
        } catch(Exception ex) {
            System.err.println("You need flags.jar in your classpath to see the flag icons.");
        }
        for(int i=0; icons != null && i<vertices.length; i++) {
            imageMap.put(vertices[i],icons[i%icons.length]);
        }
    }
    /**
     * a driver for this demo
     */
    public static void main(String[] args) 
    {
        new UnicodeLabelDemo();
    }
}
