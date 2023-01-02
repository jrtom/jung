package edu.uci.ics.jung.samples.util;

import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import java.awt.*;
import javax.swing.*;

/**
 * @author Tom Nelson
 */
public class ControlHelpers {

  public static JComponent getZoomControls(VisualizationServer vv, String title) {

    final ScalingControl scaler = new CrossoverScalingControl();
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel zoomPanel = new JPanel();
    zoomPanel.setBorder(BorderFactory.createTitledBorder(title));
    zoomPanel.add(plus);
    zoomPanel.add(minus);

    return zoomPanel;
  }

  public static JComponent getZoomControls(
      VisualizationServer vv, String title, LayoutManager buttonContainerLayoutManager) {

    final ScalingControl scaler = new CrossoverScalingControl();
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel zoomPanel = new JPanel(buttonContainerLayoutManager);
    zoomPanel.setBorder(BorderFactory.createTitledBorder(title));
    zoomPanel.add(plus);
    zoomPanel.add(minus);

    return zoomPanel;
  }

  public static JComponent getModeControls(VisualizationViewer vv, String title) {
    final DefaultModalGraphMouse<Integer, Number> graphMouse =
        new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(graphMouse);

    JPanel modePanel = new JPanel(new GridLayout(2, 1));
    modePanel.setBorder(BorderFactory.createTitledBorder(title));
    modePanel.add(graphMouse.getModeComboBox());
    return modePanel;
  }
}
