/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 7, 2003
 *
 */
package edu.uci.ics.jung.visualization

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import com.google.common.graph.Network
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.LayoutChangeListener
import edu.uci.ics.jung.layout.util.LayoutEvent
import edu.uci.ics.jung.layout.util.LayoutEventSupport
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport
import java.awt.Dimension
import java.util.function.Function
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import org.slf4j.LoggerFactory

/**
 * @author Tom Nelson
 */
open class BaseVisualizationModel<N : Any, E : Any> :
    VisualizationModel<N, E>,
    ChangeEventSupport,
    LayoutEventSupport<N>,
    LayoutChangeListener<N>,
    ChangeListener,
    LayoutModel.ChangeListener {

    private var network: Network<N, E>
    private var layoutModel: LayoutModel<N>
    private var layoutAlgorithm: LayoutAlgorithm<N>?
    private val changeSupport: ChangeEventSupport = DefaultChangeEventSupport(this)
    private val layoutChangeListeners: MutableList<LayoutChangeListener<N>> = Lists.newArrayList()

    constructor(other: VisualizationModel<N, E>) :
        this(other.getNetwork(), other.getLayoutAlgorithm(), null, other.getLayoutSize())

    constructor(other: VisualizationModel<N, E>, layoutSize: Dimension) :
        this(other.getNetwork(), other.getLayoutAlgorithm(), null, layoutSize)

    /**
     * @param network the network to visualize
     * @param layoutAlgorithm the algorithm to apply
     * @param layoutSize the size of the layout area
     */
    constructor(
        network: Network<N, E>,
        layoutAlgorithm: LayoutAlgorithm<N>?,
        layoutSize: Dimension
    ) : this(network, layoutAlgorithm, null, layoutSize)

    /**
     * Creates an instance for `graph` which initializes the node locations using `initializer`
     * and sets the layoutSize of the layout to `layoutSize`.
     *
     * @param network the graph on which the layout algorithm is to operate
     * @param initializer specifies the starting positions of the nodes
     * @param layoutSize the dimensions of the region in which the layout algorithm will place nodes
     */
    constructor(
        network: Network<N, E>,
        layoutAlgorithm: LayoutAlgorithm<N>?,
        initializer: Function<N, Point>?,
        layoutSize: Dimension
    ) {
        Preconditions.checkNotNull(network)
        Preconditions.checkNotNull(layoutSize)
        Preconditions.checkArgument(layoutSize.width > 0, "width must be > 0")
        Preconditions.checkArgument(layoutSize.height > 0, "height must be > 0")
        this.layoutAlgorithm = layoutAlgorithm
        this.layoutModel = LoadingCacheLayoutModel.builder<N>()
            .setGraph(network.asGraph())
            .setSize(layoutSize.width, layoutSize.height)
            .setInitializer(
                RandomLocationTransformer<N>(
                    layoutSize.width.toDouble(), layoutSize.height.toDouble(), System.currentTimeMillis()
                )
            )
            .build()

        if (this.layoutModel is LayoutModel.ChangeSupport) {
            (layoutModel as LayoutModel.ChangeSupport).addChangeListener(this)
        }
        if (layoutModel is LayoutEventSupport<*>) {
            @Suppress("UNCHECKED_CAST")
            (layoutModel as LayoutEventSupport<N>).addLayoutChangeListener(this)
        }
        this.network = network
        if (initializer != null) {
            this.layoutModel.setInitializer(initializer)
        }
        if (layoutAlgorithm != null) {
            this.layoutModel.accept(layoutAlgorithm)
        }
    }

    constructor(
        network: Network<N, E>,
        layoutModel: LayoutModel<N>,
        layoutAlgorithm: LayoutAlgorithm<N>?
    ) {
        Preconditions.checkNotNull(network)
        Preconditions.checkNotNull(layoutModel)
        this.layoutModel = layoutModel
        if (this.layoutModel is ChangeEventSupport) {
            (layoutModel as ChangeEventSupport).addChangeListener(this)
        }
        this.network = network
        if (layoutAlgorithm != null) {
            this.layoutModel.accept(layoutAlgorithm)
        }
        this.layoutAlgorithm = layoutAlgorithm
    }

    override fun getLayoutModel(): LayoutModel<N> {
        log.trace("getting a layoutModel $layoutModel")
        return layoutModel
    }

    override fun setLayoutModel(layoutModel: LayoutModel<N>) {
        // stop any Relaxer threads before abandoning the previous LayoutModel
        this.layoutModel.stopRelaxer()
        this.layoutModel = layoutModel
        val currentAlgorithm = layoutAlgorithm
        if (currentAlgorithm != null) {
            layoutModel.accept(currentAlgorithm)
        }
    }

    override fun setLayoutAlgorithm(layoutAlgorithm: LayoutAlgorithm<N>) {
        this.layoutAlgorithm = layoutAlgorithm
        log.trace("setLayoutAlgorithm to $layoutAlgorithm")
        layoutModel.accept(layoutAlgorithm)
    }

    /**
     * Returns the current layoutSize of the visualization space, according to the last call to
     * resize().
     *
     * @return the current layoutSize of the screen
     */
    override fun getLayoutSize(): Dimension =
        Dimension(layoutModel.width, layoutModel.height)

    override fun setNetwork(network: Network<N, E>) {
        setNetwork(network, true)
    }

    override fun setNetwork(network: Network<N, E>, forceUpdate: Boolean) {
        log.trace("setNetwork to n:{} e:{}", network.nodes(), network.edges())
        this.network = network
        this.layoutModel.graph = network.asGraph()
        val currentAlgorithm = this.layoutAlgorithm
        if (forceUpdate && currentAlgorithm != null) {
            log.trace("will accept {}", currentAlgorithm)
            layoutModel.accept(currentAlgorithm)
            log.trace("will fire stateChanged")
            changeSupport.fireStateChanged()
            log.trace("fired stateChanged")
        }
    }

    override fun getLayoutAlgorithm(): LayoutAlgorithm<N> = layoutAlgorithm!!

    override fun getNetwork(): Network<N, E> = this.network

    override fun addChangeListener(l: ChangeListener) {
        this.changeSupport.addChangeListener(l)
    }

    override fun removeChangeListener(l: ChangeListener) {
        this.changeSupport.removeChangeListener(l)
    }

    override fun getChangeListeners(): Array<ChangeListener> =
        changeSupport.getChangeListeners()

    override fun fireStateChanged() {
        this.changeSupport.fireStateChanged()
    }

    override fun addLayoutChangeListener(listener: LayoutChangeListener<N>) {
        this.layoutChangeListeners.add(listener)
    }

    override fun removeLayoutChangeListener(listener: LayoutChangeListener<N>) {
        this.layoutChangeListeners.remove(listener)
    }

    private fun fireLayoutChanged(layoutEvent: LayoutEvent<N>, network: Network<N, E>) {
        if (layoutChangeListeners.isNotEmpty()) {
            val evt: LayoutEvent<N> = LayoutNetworkEvent(layoutEvent, network)
            for (listener in layoutChangeListeners) {
                listener.layoutChanged(evt)
            }
        }
    }

    /** this is the event from the LayoutModel */
    override fun changed() {
        this.fireStateChanged()
    }

    override fun stateChanged(e: ChangeEvent) {
        this.fireStateChanged()
    }

    override fun layoutChanged(evt: LayoutEvent<N>) {
        fireLayoutChanged(evt, network)
    }

    override fun layoutChanged(evt: LayoutNetworkEvent<N>) {
        fireLayoutChanged(evt, network)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BaseVisualizationModel::class.java)
    }
}
