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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;

/**
 * Demonstrates jung support for drawing edge labels that
 * can be positioned at any point along the edge, and can
 * be rotated to be parallel with the edge.
 * 
 * @author Tom Nelson
 * 
 */
public class EdgeLabelDemo extends JApplet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6077157664507049647L;

	/**
     * the graph
     */
    Graph<Integer,Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Integer,Number> vv;
    
    /**
     */
    VertexLabelRenderer vertexLabelRenderer;
    EdgeLabelRenderer edgeLabelRenderer;
    
    ScalingControl scaler = new CrossoverScalingControl();
    
    /**
     * create an instance of a simple graph with controls to
     * demo the label positioning features
     * 
     */
    @SuppressWarnings("serial")
	public EdgeLabelDemo() {
        
        // create a simple graph for the demo
        graph = new SparseMultigraph<Integer,Number>();
        Integer[] v = createVertices(3);
        createEdges(v);
        
        Layout<Integer,Number> layout = new CircleLayout<Integer,Number>(graph);
        vv =  new VisualizationViewer<Integer,Number>(layout, new Dimension(600,400));
        vv.setBackground(Color.white);

        vertexLabelRenderer = vv.getRenderContext().getVertexLabelRenderer();
        edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer();
        
        Transformer<Number,String> stringer = new Transformer<Number,String>(){
            public String transform(Number e) {
                return "Edge:"+graph.getEndpoints(e).toString();
            }
        };
        vv.getRenderContext().setEdgeLabelTransformer(stringer);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.cyan));
        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.red, Color.yellow));
        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Integer>());
        
        // create a frome to hold the graph
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        Container content = getContentPane();
        content.add(panel);
        
        final DefaultModalGraphMouse<Integer,Number> graphMouse = new DefaultModalGraphMouse<Integer,Number>();
        vv.setGraphMouse(graphMouse);
        
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
        
        ButtonGroup radio = new ButtonGroup();
        JRadioButton lineButton = new JRadioButton("Line");
        lineButton.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Integer,Number>());
                    vv.repaint();
                }
            }
        });
        
        JRadioButton quadButton = new JRadioButton("QuadCurve");
        quadButton.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<Integer,Number>());
                    vv.repaint();
                }
            }
        });
        
        JRadioButton cubicButton = new JRadioButton("CubicCurve");
        cubicButton.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.CubicCurve<Integer,Number>());
                    vv.repaint();
                }
            }
        });
        radio.add(lineButton);
        radio.add(quadButton);
        radio.add(cubicButton);

        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        
        JCheckBox rotate = new JCheckBox("<html><center>EdgeType<p>Parallel</center></html>");
        rotate.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                AbstractButton b = (AbstractButton)e.getSource();
                edgeLabelRenderer.setRotateEdgeLabels(b.isSelected());
                vv.repaint();
            }
        });
        rotate.setSelected(true);
        MutableDirectionalEdgeValue mv = new MutableDirectionalEdgeValue(.5, .7);
        vv.getRenderContext().setEdgeLabelClosenessTransformer(mv);
        JSlider directedSlider = new JSlider(mv.getDirectedModel()) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width /= 2;
                return d;
            }
        };
        JSlider undirectedSlider = new JSlider(mv.getUndirectedModel()) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width /= 2;
                return d;
            }
        };
        
        JSlider edgeOffsetSlider = new JSlider(0,50) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width /= 2;
                return d;
            }
        };
        edgeOffsetSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider s = (JSlider)e.getSource();
                AbstractEdgeShapeTransformer<Integer,Number> aesf = 
                    (AbstractEdgeShapeTransformer<Integer,Number>)vv.getRenderContext().getEdgeShapeTransformer();
                aesf.setControlOffsetIncrement(s.getValue());
                vv.repaint();
            }
        	
        });
        
        Box controls = Box.createHorizontalBox();

        JPanel zoomPanel = new JPanel(new GridLayout(0,1));
        zoomPanel.setBorder(BorderFactory.createTitledBorder("Scale"));
        zoomPanel.add(plus);
        zoomPanel.add(minus);

        JPanel edgePanel = new JPanel(new GridLayout(0,1));
        edgePanel.setBorder(BorderFactory.createTitledBorder("EdgeType Type"));
        edgePanel.add(lineButton);
        edgePanel.add(quadButton);
        edgePanel.add(cubicButton);

        JPanel rotatePanel = new JPanel();
        rotatePanel.setBorder(BorderFactory.createTitledBorder("Alignment"));
        rotatePanel.add(rotate);

        JPanel labelPanel = new JPanel(new BorderLayout());
        JPanel sliderPanel = new JPanel(new GridLayout(3,1));
        JPanel sliderLabelPanel = new JPanel(new GridLayout(3,1));
        JPanel offsetPanel = new JPanel(new BorderLayout());
        offsetPanel.setBorder(BorderFactory.createTitledBorder("Offset"));
        sliderPanel.add(directedSlider);
        sliderPanel.add(undirectedSlider);
        sliderPanel.add(edgeOffsetSlider);
        sliderLabelPanel.add(new JLabel("Directed", JLabel.RIGHT));
        sliderLabelPanel.add(new JLabel("Undirected", JLabel.RIGHT));
        sliderLabelPanel.add(new JLabel("Edges", JLabel.RIGHT));
        offsetPanel.add(sliderLabelPanel, BorderLayout.WEST);
        offsetPanel.add(sliderPanel);
        labelPanel.add(offsetPanel);
        labelPanel.add(rotatePanel, BorderLayout.WEST);
        
        JPanel modePanel = new JPanel(new GridLayout(2,1));
        modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        modePanel.add(graphMouse.getModeComboBox());

        controls.add(zoomPanel);
        controls.add(edgePanel);
        controls.add(labelPanel);
        controls.add(modePanel);
        content.add(controls, BorderLayout.SOUTH);
        quadButton.setSelected(true);
    }
    
    /**
     * subclassed to hold two BoundedRangeModel instances that
     * are used by JSliders to move the edge label positions
     * @author Tom Nelson
     *
     *
     */
    class MutableDirectionalEdgeValue extends ConstantDirectionalEdgeValueTransformer<Integer,Number> {
        BoundedRangeModel undirectedModel = new DefaultBoundedRangeModel(5,0,0,10);
        BoundedRangeModel directedModel = new DefaultBoundedRangeModel(7,0,0,10);
        
        public MutableDirectionalEdgeValue(double undirected, double directed) {
            super(undirected, directed);
            undirectedModel.setValue((int)(undirected*10));
            directedModel.setValue((int)(directed*10));
            
            undirectedModel.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    setUndirectedValue(new Double(undirectedModel.getValue()/10f));
                    vv.repaint();
                }
            });
            directedModel.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    setDirectedValue(new Double(directedModel.getValue()/10f));
                    vv.repaint();
                }
            });
        }
        /**
         * @return Returns the directedModel.
         */
        public BoundedRangeModel getDirectedModel() {
            return directedModel;
        }

        /**
         * @return Returns the undirectedModel.
         */
        public BoundedRangeModel getUndirectedModel() {
            return undirectedModel;
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
        graph.addEdge(new Double(Math.random()), v[0], v[1], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[1], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[0], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[0], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[2]);
        graph.addEdge(new Double(Math.random()), v[1], v[2]);
    }

    /**
     * a driver for this demo
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = frame.getContentPane();
        content.add(new EdgeLabelDemo());
        frame.pack();
        frame.setVisible(true);
    }
}
