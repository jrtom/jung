/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.generators.random.MixedRandomGraphGenerator;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;


/**
 * Demonstrates several of the graph layout algorithms.
 * Allows the user to interactively select one of several graphs, and one of
 * several layouts, and visualizes the combination.
 * 
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
@SuppressWarnings("serial")
public class ShowLayouts extends JApplet {
    protected static Graph<? extends Object, ? extends Object>[] g_array;
    protected static int graph_index;
    protected static String[] graph_names = {"Two component graph", 
        "Random mixed-mode graph", "Miscellaneous multicomponent graph", 
        "Random directed acyclic graph", "One component graph", 
        "Chain+isolate graph", "Trivial (disconnected) graph"};
    
	public static class GraphChooser implements ActionListener
    {
        private JComboBox layout_combo;

        public GraphChooser(JComboBox layout_combo)
        {
            this.layout_combo = layout_combo;
        }
        
        public void actionPerformed(ActionEvent e)
        {
            JComboBox cb = (JComboBox)e.getSource();
            graph_index = cb.getSelectedIndex();
            layout_combo.setSelectedIndex(layout_combo.getSelectedIndex()); // rebuild the layout
        }
    }

    /**
	 * 
	 * @author danyelf
	 */
	
	private static final class LayoutChooser implements ActionListener
    {
        private final JComboBox jcb;
        private final VisualizationViewer<Integer,Number> vv;

        private LayoutChooser(JComboBox jcb, VisualizationViewer<Integer,Number> vv)
        {
            super();
            this.jcb = jcb;
            this.vv = vv;
        }

        public void actionPerformed(ActionEvent arg0)
        {
            Object[] constructorArgs =
                { g_array[graph_index]};

            Class<? extends Layout<Integer,Number>> layoutC = 
                (Class<? extends Layout<Integer,Number>>) jcb.getSelectedItem();
//            Class lay = layoutC;
            try
            {
                Constructor<? extends Layout<Integer, Number>> constructor = layoutC
                        .getConstructor(new Class[] {Graph.class});
                Object o = constructor.newInstance(constructorArgs);
                Layout<Integer,Number> l = (Layout<Integer,Number>) o;
                l.setInitializer(vv.getGraphLayout());
                l.setSize(vv.getSize());
                
				LayoutTransition<Integer,Number> lt =
					new LayoutTransition<Integer,Number>(vv, vv.getGraphLayout(), l);
				Animator animator = new Animator(lt);
				animator.start();
				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
				vv.repaint();
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static JPanel getGraphPanel()
    {
        g_array = 
            (Graph<? extends Object,? extends Object>[])
            new Graph<?,?>[graph_names.length];
        
    	Factory<Graph<Integer,Number>> graphFactory =
    		new Factory<Graph<Integer,Number>>() {
    		public Graph<Integer,Number> create() {
    			return new SparseMultigraph<Integer,Number>();
    		}
    	};

    	Factory<Integer> vertexFactory = new Factory<Integer>() {
    			int count;
				public Integer create() {
					return count++;
				}};
		Factory<Number> edgeFactory = new Factory<Number>() {
			int count;
				public Number create() {
					return count++;
				}};

            
        g_array[0] = TestGraphs.createTestGraph(false);
        g_array[1] = MixedRandomGraphGenerator.generateMixedRandomGraph(graphFactory, 
        		vertexFactory, edgeFactory, new HashMap<Number,Number>(), 20, true, new HashSet<Integer>());
        g_array[2] = TestGraphs.getDemoGraph();
        g_array[3] = TestGraphs.createDirectedAcyclicGraph(4, 4, 0.3);
        g_array[4] = TestGraphs.getOneComponentGraph();
        g_array[5] = TestGraphs.createChainPlusIsolates(18, 5);
        g_array[6] = TestGraphs.createChainPlusIsolates(0, 20);

        Graph<? extends Object, ? extends Object> g = g_array[4]; // initial graph

        final VisualizationViewer<Integer,Number> vv = 
            new VisualizationViewer<Integer,Number>(new FRLayout(g));
        
        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.red, Color.yellow));
        
        final DefaultModalGraphMouse<Integer,Number> graphMouse = new DefaultModalGraphMouse<Integer,Number>();
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
        JButton reset = new JButton("reset");
        reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Layout<Integer,Number> layout = vv.getGraphLayout();
				layout.initialize();
				Relaxer relaxer = vv.getModel().getRelaxer();
				if(relaxer != null) {
//				if(layout instanceof IterativeContext) {
					relaxer.stop();
					relaxer.prerelax();
					relaxer.relax();
				}
			}});
        
        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(((DefaultModalGraphMouse<Integer,Number>)vv.getGraphMouse()).getModeListener());

        JPanel jp = new JPanel();
        jp.setBackground(Color.WHITE);
        jp.setLayout(new BorderLayout());
        jp.add(vv, BorderLayout.CENTER);
        Class[] combos = getCombos();
        final JComboBox jcb = new JComboBox(combos);
        // use a renderer to shorten the layout name presentation
        jcb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        jcb.addActionListener(new LayoutChooser(jcb, vv));
        jcb.setSelectedItem(FRLayout.class);

        JPanel control_panel = new JPanel(new GridLayout(2,1));
        JPanel topControls = new JPanel();
        JPanel bottomControls = new JPanel();
        control_panel.add(topControls);
        control_panel.add(bottomControls);
        jp.add(control_panel, BorderLayout.NORTH);
        
        final JComboBox graph_chooser = new JComboBox(graph_names);
        
        graph_chooser.addActionListener(new GraphChooser(jcb));
        
        topControls.add(jcb);
        topControls.add(graph_chooser);
        bottomControls.add(plus);
        bottomControls.add(minus);
        bottomControls.add(modeBox);
        bottomControls.add(reset);
        return jp;
    }

    public void start()
    {
        this.getContentPane().add(getGraphPanel());
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Layout>[] getCombos()
    {
        List<Class<? extends Layout>> layouts = new ArrayList<Class<? extends Layout>>();
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        layouts.add(SpringLayout2.class);
        layouts.add(ISOMLayout.class);
        return layouts.toArray(new Class[0]);
    }

    public static void main(String[] args)
    {
        JPanel jp = getGraphPanel();

        JFrame jf = new JFrame();
        jf.getContentPane().add(jp);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }
}
