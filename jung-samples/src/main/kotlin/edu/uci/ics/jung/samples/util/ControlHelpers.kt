package edu.uci.ics.jung.samples.util

import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import java.awt.GridLayout
import java.awt.LayoutManager
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Tom Nelson
 */
object ControlHelpers {

    @JvmStatic
    fun getZoomControls(vv: VisualizationServer<*, *>, title: String): JComponent {
        val scaler = CrossoverScalingControl()
        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }
        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1.0f / 1.1f, vv.getCenter()) }

        val zoomPanel = JPanel()
        zoomPanel.border = BorderFactory.createTitledBorder(title)
        zoomPanel.add(plus)
        zoomPanel.add(minus)

        return zoomPanel
    }

    @JvmStatic
    fun getZoomControls(
        vv: VisualizationServer<*, *>,
        title: String,
        buttonContainerLayoutManager: LayoutManager
    ): JComponent {
        val scaler = CrossoverScalingControl()
        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }
        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1.0f / 1.1f, vv.getCenter()) }

        val zoomPanel = JPanel(buttonContainerLayoutManager)
        zoomPanel.border = BorderFactory.createTitledBorder(title)
        zoomPanel.add(plus)
        zoomPanel.add(minus)

        return zoomPanel
    }

    @JvmStatic
    fun getModeControls(vv: VisualizationViewer<*, *>, title: String): JComponent {
        val graphMouse = DefaultModalGraphMouse<Int, Number>()
        vv.setGraphMouse(graphMouse)

        val modePanel = JPanel(GridLayout(2, 1))
        modePanel.border = BorderFactory.createTitledBorder(title)
        modePanel.add(graphMouse.getModeComboBox())
        return modePanel
    }
}
