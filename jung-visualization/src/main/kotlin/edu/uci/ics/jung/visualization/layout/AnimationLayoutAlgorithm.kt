package edu.uci.ics.jung.visualization.layout

import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.algorithms.AbstractIterativeLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import edu.uci.ics.jung.visualization.VisualizationServer
import org.slf4j.LoggerFactory

/**
 * @author Tom Nelson
 */
open class AnimationLayoutAlgorithm<N : Any>(
    private val visualizationServer: VisualizationServer<N, *>,
    private val endLayoutAlgorithm: LayoutAlgorithm<N>
) : AbstractIterativeLayoutAlgorithm<N>(), IterativeContext {

    private var done: Boolean = false
    private val count: Int = 20
    private var counter: Int = 0

    private lateinit var transitionLayoutModel: LayoutModel<N>
    override lateinit var layoutModel: LayoutModel<N>

    init {
        this.shouldPreRelax = false
    }

    override fun visit(layoutModel: LayoutModel<N>) {
        // save off the existing layoutModel
        this.layoutModel = layoutModel
        // create a LayoutModel to hold points for the transition
        this.transitionLayoutModel = LoadingCacheLayoutModel.builder<N>()
            .setGraph(visualizationServer.getModel().getNetwork().asGraph())
            .setLayoutModel(layoutModel)
            .setInitializer(layoutModel)
            .build()
        // start off the transitionLayoutModel with the endLayoutAlgorithm
        transitionLayoutModel.accept(endLayoutAlgorithm)
    }

    /**
     * each step of the animation moves every point 1/count of the distance from its old location to
     * its new location
     */
    override fun step() {
        for (v in layoutModel.graph.nodes()) {
            val tp = layoutModel.apply(v)
            val fp = transitionLayoutModel.apply(v)
            val dx = (fp.x - tp.x) / (count - counter)
            val dy = (fp.y - tp.y) / (count - counter)
            log.trace("dx:{},dy:{}", dx, dy)
            layoutModel.set(v, tp.x + dx, tp.y + dy)
        }
        counter++
        if (counter >= count) {
            done = true
            this.transitionLayoutModel.stopRelaxer()
            this.visualizationServer.getModel().setLayoutAlgorithm(endLayoutAlgorithm)
        }
    }

    override fun done(): Boolean = done

    companion object {
        private val log = LoggerFactory.getLogger(AnimationLayoutAlgorithm::class.java)
    }
}
