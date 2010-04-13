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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexIconTransformer;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.shape.MagnifyImageLensSupport;


/**
 * Demonstrates the use of images to represent graph vertices.
 * The images are added to the DefaultGraphLabelRenderer and can
 * either be offset from the vertex, or centered on the vertex.
 * Additionally, the relative positioning of the label and
 * image is controlled by subclassing the DefaultGraphLabelRenderer
 * and setting the appropriate properties on its JLabel superclass
 *  FancyGraphLabelRenderer
 * 
 * The images used in this demo (courtesy of slashdot.org) are
 * rectangular but with a transparent background. When vertices
 * are represented by these images, it looks better if the actual
 * shape of the opaque part of the image is computed so that the
 * edge arrowheads follow the visual shape of the image. This demo
 * uses the FourPassImageShaper class to compute the Shape from
 * an image with transparent background.
 * 
 * @author Tom Nelson
 * 
 */
public class LensVertexImageShaperDemo extends JApplet {

	   /**
	 * 
	 */
	private static final long serialVersionUID = 5432239991020505763L;

	/**
     * the graph
     */
    DirectedSparseGraph<Number, Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Number,Number> vv;
    
    /**
     * some icon names to use
     */
    String[] iconNames = {
            "apple",
            "os",
            "x",
            "linux",
            "inputdevices",
            "wireless",
            "graphics3",
            "gamespcgames",
            "humor",
            "music",
            "privacy"
    };
    
    LensSupport viewSupport;
    LensSupport modelSupport;
    LensSupport magnifyLayoutSupport;
    LensSupport magnifyViewSupport;
    /**
     * create an instance of a simple graph with controls to
     * demo the zoom features.
     * 
     */
    public LensVertexImageShaperDemo() {
        
        // create a simple graph for the demo
        graph = new DirectedSparseGraph<Number,Number>();
        Number[] vertices = createVertices(11);
        
        // a Map for the labels
        Map<Number,String> map = new HashMap<Number,String>();
        for(int i=0; i<vertices.length; i++) {
            map.put(vertices[i], iconNames[i%iconNames.length]);
        }
        
        // a Map for the Icons
        Map<Number,Icon> iconMap = new HashMap<Number,Icon>();
        for(int i=0; i<vertices.length; i++) {
            String name = "/images/topic"+iconNames[i]+".gif";
            try {
                Icon icon = 
                    new LayeredIcon(new ImageIcon(LensVertexImageShaperDemo.class.getResource(name)).getImage());
                iconMap.put(vertices[i], icon);
            } catch(Exception ex) {
                System.err.println("You need slashdoticons.jar in your classpath to see the image "+name);
            }
        }
        
        createEdges(vertices);
        
        FRLayout<Number, Number> layout = new FRLayout<Number, Number>(graph);
        layout.setMaxIterations(100);
        vv =  new VisualizationViewer<Number, Number>(layout, new Dimension(600,600));
        
        Transformer<Number,Paint> vpf = 
            new PickableVertexPaintTransformer<Number>(vv.getPickedVertexState(), Color.white, Color.yellow);
        vv.getRenderContext().setVertexFillPaintTransformer(vpf);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.cyan));

        vv.setBackground(Color.white);
        
        final Transformer<Number,String> vertexStringerImpl = 
            new VertexStringerImpl<Number>(map);
        vv.getRenderContext().setVertexLabelTransformer(vertexStringerImpl);
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
        
        
        // features on and off. For a real application, use VertexIconAndShapeFunction instead.
        final VertexIconShapeTransformer<Number> vertexImageShapeFunction =
            new VertexIconShapeTransformer<Number>(new EllipseVertexShapeTransformer<Number>());

        final DefaultVertexIconTransformer<Number> vertexIconFunction =
        	new DefaultVertexIconTransformer<Number>();
        
        vertexImageShapeFunction.setIconMap(iconMap);
        vertexIconFunction.setIconMap(iconMap);
        
        vv.getRenderContext().setVertexShapeTransformer(vertexImageShapeFunction);
        vv.getRenderContext().setVertexIconTransformer(vertexIconFunction);

        
        // Get the pickedState and add a listener that will decorate the
        // Vertex images with a checkmark icon when they are picked
        PickedState<Number> ps = vv.getPickedVertexState();
        ps.addItemListener(new PickWithIconListener(vertexIconFunction));
        
        vv.addPostRenderPaintable(new VisualizationViewer.Paintable(){
            int x;
            int y;
            Font font;
            FontMetrics metrics;
            int swidth;
            int sheight;
            String str = "Thank You, slashdot.org, for the images!";
            
            public void paint(Graphics g) {
                Dimension d = vv.getSize();
                if(font == null) {
                    font = new Font(g.getFont().getName(), Font.BOLD, 20);
                    metrics = g.getFontMetrics(font);
                    swidth = metrics.stringWidth(str);
                    sheight = metrics.getMaxAscent()+metrics.getMaxDescent();
                    x = (d.width-swidth)/2;
                    y = (int)(d.height-sheight*1.5);
                }
                g.setFont(font);
                Color oldColor = g.getColor();
                g.setColor(Color.lightGray);
                g.drawString(str, x, y);
                g.setColor(oldColor);
            }
            public boolean useTransform() {
                return false;
            }
        });

        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Number>());
        
        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);
        
        
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
        
        JComboBox modeBox = graphMouse.getModeComboBox();
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        modePanel.add(modeBox);
        
        JPanel scaleGrid = new JPanel(new GridLayout(1,0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        controls.add(scaleGrid);

        controls.add(modePanel);
        content.add(controls, BorderLayout.SOUTH);
        
        this.viewSupport = new MagnifyImageLensSupport<Number,Number>(vv);
//        	new ViewLensSupport<Number,Number>(vv, new HyperbolicShapeTransformer(vv, 
//        		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)), 
//                new ModalLensGraphMouse());

        this.modelSupport = new LayoutLensSupport<Number,Number>(vv);
        
        graphMouse.addItemListener(modelSupport.getGraphMouse().getModeListener());
        graphMouse.addItemListener(viewSupport.getGraphMouse().getModeListener());

        ButtonGroup radio = new ButtonGroup();
        JRadioButton none = new JRadioButton("None");
        none.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if(viewSupport != null) {
                    viewSupport.deactivate();
                }
                if(modelSupport != null) {
                    modelSupport.deactivate();
                }
            }
        });
        none.setSelected(true);

        JRadioButton hyperView = new JRadioButton("View");
        hyperView.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                viewSupport.activate(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        JRadioButton hyperModel = new JRadioButton("Layout");
        hyperModel.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                modelSupport.activate(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        radio.add(none);
        radio.add(hyperView);
        radio.add(hyperModel);
        
        JMenuBar menubar = new JMenuBar();
        JMenu modeMenu = graphMouse.getModeMenu();
        menubar.add(modeMenu);

        JPanel lensPanel = new JPanel(new GridLayout(2,0));
        lensPanel.setBorder(BorderFactory.createTitledBorder("Lens"));
        lensPanel.add(none);
        lensPanel.add(hyperView);
        lensPanel.add(hyperModel);
        controls.add(lensPanel);
    }
    
    /**
     * A simple implementation of VertexStringer that
     * gets Vertex labels from a Map  
     * 
     * @author Tom Nelson 
     *
     *
     */
    class VertexStringerImpl<V> implements Transformer<V,String> {

        Map<V,String> map = new HashMap<V,String>();
        
        boolean enabled = true;
        
        public VertexStringerImpl(Map<V,String> map) {
            this.map = map;
        }
        
        /**
         * @see edu.uci.ics.jung.graph.decorators.VertexStringer#getLabel(edu.uci.ics.jung.graph.Vertex)
         */
        public String transform(V v) {
            if(isEnabled()) {
                return map.get(v);
            } else {
                return "";
            }
        }

        /**
         * @return Returns the enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @param enabled The enabled to set.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * create some vertices
     * @param count how many to create
     * @return the Vertices in an array
     */
    private Number[] createVertices(int count) {
        Number[] v = new Number[count];
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
    void createEdges(Number[] v) {
        graph.addEdge(new Double(Math.random()), v[0], v[1], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[3], v[0], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[4], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[5], v[3], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[2], v[1], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[1], EdgeType.DIRECTED);
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
        graph.addEdge(new Double(Math.random()), v[4], v[10], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[10], v[4], EdgeType.DIRECTED);
    }
    
    public static class PickWithIconListener implements ItemListener {
        DefaultVertexIconTransformer<Number> imager;
        Icon checked;
        
        public PickWithIconListener(DefaultVertexIconTransformer<Number> imager) {
            this.imager = imager;
            checked = new Checkmark(Color.red);
        }

        public void itemStateChanged(ItemEvent e) {
            Icon icon = imager.transform((Number)e.getItem());
            if(icon != null && icon instanceof LayeredIcon) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    ((LayeredIcon)icon).add(checked);
                } else {
                    ((LayeredIcon)icon).remove(checked);
                }
            }
        }
    }


    /**
     * a driver for this demo
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        content.add(new LensVertexImageShaperDemo());
        frame.pack();
        frame.setVisible(true);
    }
}
