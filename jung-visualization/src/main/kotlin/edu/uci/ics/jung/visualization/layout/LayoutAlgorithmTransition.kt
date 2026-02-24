package edu.uci.ics.jung.visualization.layout

import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.visualization.VisualizationServer
import org.slf4j.LoggerFactory

/**
 * Manages the transition to a new LayoutAlgorithm. The transition can be animated or immediate. The
 * view side has a reference to the VisualizationServer so that it can manage activity of the
 * Spatial structures during the transition. Typically, they are turned off until the transition is
 * complete to minimize unnecessary work.
 */
object LayoutAlgorithmTransition {

    private val log = LoggerFactory.getLogger(LayoutAlgorithmTransition::class.java)

    @JvmStatic
    fun <N : Any, E : Any> animate(
        visualizationServer: VisualizationServer<N, E>, endLayoutAlgorithm: LayoutAlgorithm<N>
    ) {
        fireLayoutStateChanged(visualizationServer.getModel().getLayoutModel(), true)
        val transitionLayoutAlgorithm: LayoutAlgorithm<N> =
            AnimationLayoutAlgorithm(visualizationServer, endLayoutAlgorithm)
        visualizationServer.getModel().setLayoutAlgorithm(transitionLayoutAlgorithm)
    }

    @JvmStatic
    fun <N : Any, E : Any> apply(
        visualizationServer: VisualizationServer<N, E>, endLayoutAlgorithm: LayoutAlgorithm<N>
    ) {
        visualizationServer.getModel().setLayoutAlgorithm(endLayoutAlgorithm)
    }

    private fun fireLayoutStateChanged(layoutModel: LayoutModel<*>, state: Boolean) {
        log.trace("fireLayoutStateChanged to {}", state)
        layoutModel.layoutStateChangeSupport.fireLayoutStateChanged(layoutModel, state)
    }
}
