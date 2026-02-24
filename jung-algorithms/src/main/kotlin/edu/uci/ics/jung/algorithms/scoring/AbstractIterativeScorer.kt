/*
 * Created on Jul 6, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.scoring.util.DelegateToEdgeTransformer
import edu.uci.ics.jung.algorithms.scoring.util.VEPair
import edu.uci.ics.jung.algorithms.util.IterativeContext
import java.util.Collections
import java.util.function.Function

/**
 * An abstract class for algorithms that assign scores to nodes based on iterative methods.
 * Generally, any (concrete) subclass will function by creating an instance, and then either calling
 * `evaluate` (if the user wants to iterate until the algorithms is 'done') or repeatedly
 * call `step` (if the user wants to observe the values at each step).
 */
abstract class AbstractIterativeScorer<N : Any, E : Any, T>
    : IterativeContext, NodeScorer<N, T> {

  /** Maximum number of iterations to use before terminating. Defaults to 100. */
  protected var max_iterations: Int

  /**
   * Minimum change from one step to the next; if all changes are <= tolerance, no further updates
   * will occur. Defaults to 0.001.
   */
  internal var tolerance: Double

  /** The graph on which the calculations are to be made. */
  protected val graph: Network<N, E>

  /** The total number of iterations used so far. */
  protected var total_iterations: Int = 0

  /** The edge weights used by this algorithm. */
  protected var edge_weights: Function<VEPair<N, E>, out Number>? = null

  /** The map in which the output values are stored. */
  private var output: MutableMap<N, T> = HashMap()

  /** The map in which the current values are stored. */
  private var current_values: MutableMap<N, T> = HashMap()

  /**
   * A flag representing whether this instance tolerates disconnected graphs. Instances that do not
   * accept disconnected graphs may have unexpected behavior on disconnected graphs; they are not
   * guaranteed to do an explicit check. Defaults to true.
   */
  private var accept_disconnected_graph: Boolean

  /** The largest change seen so far among all node scores. */
  protected var max_delta: Double = 0.0

  /**
   * Creates an instance for the specified graph and edge weights.
   *
   * @param g the graph for which the instance is to be created
   * @param edge_weights the edge weights for this instance
   */
  constructor(g: Network<N, E>, edge_weights: Function<in E, out Number>) {
    this.graph = g
    this.max_iterations = 100
    this.tolerance = 0.001
    this.accept_disconnected_graph = true
    setEdgeWeights(edge_weights)
  }

  /**
   * Creates an instance for the specified graph `g`. NOTE: This constructor does not set
   * the internal `edge_weights` variable. If this variable is used by the subclass which
   * invoked this constructor, it must be initialized by that subclass.
   *
   * @param g the graph for which the instance is to be created
   */
  constructor(g: Network<N, E>) {
    this.graph = g
    this.max_iterations = 100
    this.tolerance = 0.001
    this.accept_disconnected_graph = true
  }

  /**
   * Sets the output value for this node.
   *
   * @param v the node whose output value is to be set
   * @param value the value to set
   */
  protected fun setOutputValue(v: N, value: T) {
    output[v] = value
  }

  /**
   * Gets the output value for this node.
   *
   * @param v the node whose output value is to be retrieved
   * @return the output value for this node
   */
  protected fun getOutputValue(v: N): T = output[v] as T

  /**
   * Gets the current value for this node
   *
   * @param v the node whose current value is to be retrieved
   * @return the current value for this node
   */
  protected fun getCurrentValue(v: N): T = current_values[v] as T

  /**
   * Sets the current value for this node.
   *
   * @param v the node whose current value is to be set
   * @param value the current value to set
   */
  protected fun setCurrentValue(v: N, value: T) {
    current_values[v] = value
  }

  /** Initializes the internal state for this instance. */
  protected open fun initialize() {
    this.total_iterations = 0
    this.max_delta = Double.MIN_VALUE
    this.current_values = HashMap()
    this.output = HashMap()
  }

  /** Steps through this scoring algorithm until a termination condition is reached. */
  fun evaluate() {
    do {
      step()
    } while (!done())
  }

  /**
   * Returns true if the total number of iterations is greater than or equal to `max_iterations`
   * or if the maximum value change observed is less than `tolerance`.
   */
  override fun done(): Boolean = total_iterations >= max_iterations || max_delta < tolerance

  /** Performs one step of this algorithm; updates the state (value) for each node. */
  override fun step() {
    swapOutputForCurrent()
    max_delta = 0.0

    for (v in graph.nodes()) {
      val diff = update(v)
      updateMaxDelta(v, diff)
    }
    total_iterations++
    afterStep()
  }

  /** */
  protected fun swapOutputForCurrent() {
    val tmp = output
    output = current_values
    current_values = tmp
  }

  /**
   * Updates the value for `v`.
   *
   * @param v the node whose value is to be updated
   * @return the updated value
   */
  protected abstract fun update(v: N): Double

  protected open fun updateMaxDelta(v: N, diff: Double) {
    max_delta = Math.max(max_delta, diff)
  }

  protected open fun afterStep() {}

  override fun getNodeScore(v: N): T {
    Preconditions.checkArgument(
      graph.nodes().contains(v), "Node %s not an element of this graph", v.toString()
    )
    return output[v] as T
  }

  override fun nodeScores(): Map<N, T> = Collections.unmodifiableMap(output)

  /**
   * Returns the maximum number of iterations that this instance will use.
   *
   * @return the maximum number of iterations that `evaluate` will use prior to terminating
   */
  fun getMaxIterations(): Int = max_iterations

  /**
   * Returns the number of iterations that this instance has used so far.
   *
   * @return the number of iterations that this instance has used so far
   */
  fun getIterations(): Int = total_iterations

  /**
   * Sets the maximum number of times that `evaluate` will call `step`.
   *
   * @param max_iterations the maximum
   */
  fun setMaxIterations(max_iterations: Int) {
    this.max_iterations = max_iterations
  }

  /**
   * Returns the Function that this instance uses to associate edge weights with each edge.
   *
   * @return the Function that associates an edge weight with each edge
   */
  fun getEdgeWeights(): Function<VEPair<N, E>, out Number>? = edge_weights

  /**
   * Sets the Function that this instance uses to associate edge weights with each edge
   *
   * @param edge_weights the Function to use to associate an edge weight with each edge
   * @see edu.uci.ics.jung.algorithms.scoring.util.UniformDegreeWeight
   */
  fun setEdgeWeights(edge_weights: Function<in E, out Number>) {
    this.edge_weights = DelegateToEdgeTransformer(edge_weights)
  }

  /**
   * Gets the edge weight for `e` in the context of its (incident) node `v`.
   *
   * @param v the node incident to e as a context in which the edge weight is to be calculated
   * @param e the edge whose weight is to be returned
   * @return the edge weight for `e` in the context of its (incident) node `v`
   */
  protected fun getEdgeWeight(v: N, e: E): Number =
    edge_weights!!.apply(VEPair(v, e))

  /**
   * Collects the 'potential' from v (its current value) if it has no outgoing edges; this can then
   * be redistributed among the other nodes as a means of normalization.
   *
   * @param v the node whose potential is being collected
   */
  protected open fun collectDisappearingPotential(v: N) {}

  /**
   * Specifies whether this instance should accept nodes with no outgoing edges.
   *
   * @param accept true if this instance should accept nodes with no outgoing edges, false otherwise
   */
  fun acceptDisconnectedGraph(accept: Boolean) {
    this.accept_disconnected_graph = accept
  }

  /**
   * Returns true if this instance accepts nodes with no outgoing edges, and false otherwise.
   *
   * @return true if this instance accepts nodes with no outgoing edges, otherwise false
   */
  fun isDisconnectedGraphOK(): Boolean = this.accept_disconnected_graph
}
