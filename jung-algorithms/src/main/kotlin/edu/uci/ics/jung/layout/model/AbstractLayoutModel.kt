package edu.uci.ics.jung.layout.model

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.graph.Graph
import edu.uci.ics.jung.layout.algorithms.IterativeLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.util.LayoutChangeListener
import edu.uci.ics.jung.layout.util.LayoutEvent
import edu.uci.ics.jung.layout.util.LayoutEventSupport
import edu.uci.ics.jung.layout.util.VisRunnable
import java.util.ConcurrentModificationException
import java.util.concurrent.CompletableFuture
import org.slf4j.LoggerFactory

/**
 * superclass for LayoutModels. Holds the required attributes for placing graph nodes in a
 * visualization
 *
 * @author Tom Nelson
 * @param N the node type
 */
abstract class AbstractLayoutModel<N : Any>(
  graph: Graph<N>,
  width: Int,
  height: Int
) : LayoutModel<N>, LayoutModel.ChangeSupport, LayoutEventSupport<N> {

  private val lockedNodes: MutableSet<N> = Sets.newHashSet()
  protected var locked: Boolean = false
  private var _width: Int = 0
  private var _height: Int = 0
  override var graph: Graph<N> = checkNotNull(graph)
    set(value) {
      field = checkNotNull(value)
      if (log.isTraceEnabled) {
        log.trace("setGraph to n:{} e:{}", value.nodes(), value.edges())
      }
    }

  protected var visRunnable: VisRunnable? = null

  /**
   * @value relaxing true is this layout model is being accessed by a running relaxer
   */
  protected var _relaxing: Boolean = false

  protected var _theFuture: CompletableFuture<*>? = null

  override val theFuture: CompletableFuture<*>?
    get() = _theFuture

  override var changeSupport: LayoutModel.ChangeSupport = DefaultLayoutModelChangeSupport()
  private val layoutChangeListeners: MutableList<LayoutChangeListener<N>> = Lists.newArrayList()
  override var layoutStateChangeSupport: LayoutModel.LayoutStateChangeSupport =
    DefaultLayoutStateChangeSupport()

  init {
    setSize(width, height)
  }

  /** stop any running Relaxer */
  override fun stopRelaxer() {
    visRunnable?.stop()
    _theFuture?.cancel(true)
    setRelaxing(false)
  }

  /**
   * accept the visit of a LayoutAlgorithm. If it is an IterativeContext, create a VisRunner to run
   * its relaxer in a new Thread. If there is a current VisRunner, stop it first.
   *
   * @param layoutAlgorithm
   */
  override fun accept(layoutAlgorithm: LayoutAlgorithm<N>) {
    // the layoutMode is active with a new LayoutAlgorithm
    layoutStateChangeSupport.fireLayoutStateChanged(this, true)
    log.trace("accepting {}", layoutAlgorithm)
    changeSupport.isFireEvents = true
    visRunnable?.let {
      log.trace("stopping {}", it)
      it.stop()
    }
    _theFuture?.cancel(true)

    if (log.isTraceEnabled) {
      log.trace("{} will visit {}", layoutAlgorithm, this)
    }
    layoutAlgorithm.visit(this)

    if (layoutAlgorithm is IterativeLayoutAlgorithm<*>) {
      setRelaxing(true)
      @Suppress("UNCHECKED_CAST")
      setupVisRunner(layoutAlgorithm as IterativeLayoutAlgorithm<N>)
      // need to have the visRunner fire the layoutStateChanged event when it finishes
    } else {
      if (log.isTraceEnabled) {
        log.trace("no visRunner for this {}", this)
      }
      // the layout model has finished with the layout algorithm
      log.trace("will fire layoutStateCHanged with false")
      layoutStateChangeSupport.fireLayoutStateChanged(this, false)
    }
  }

  /**
   * create and start a new VisRunner for the passed IterativeContext
   *
   * @param iterativeContext
   */
  protected fun setupVisRunner(iterativeContext: IterativeLayoutAlgorithm<N>) {
    log.trace("this {} is setting up a visRunnable with {}", this, iterativeContext)
    visRunnable?.stop()
    _theFuture?.cancel(true)

    // layout becomes active
    layoutStateChangeSupport.fireLayoutStateChanged(this, true)
    // prerelax phase
    changeSupport.isFireEvents = false
    iterativeContext.preRelax()
    changeSupport.isFireEvents = true
    log.trace("prerelax is done")

    val runner = VisRunnable(iterativeContext)
    visRunnable = runner
    _theFuture = CompletableFuture.runAsync(runner)
      .thenRun {
        log.trace("We're done")
        setRelaxing(false)
        this.fireChanged()
        // fire an event to say that the layout relax is done
        this.layoutStateChangeSupport.fireLayoutStateChanged(this, false)
      }
  }

  /**
   * set locked state for the provided node
   *
   * @param node the node to affect
   * @param locked to lock or not
   */
  override fun lock(node: N, locked: Boolean) {
    if (locked) {
      lockedNodes.add(node)
    } else {
      lockedNodes.remove(node)
    }
  }

  /**
   * @param node the node whose locked state is being queried
   * @return whether the passed node is locked
   */
  override fun isLocked(node: N): Boolean = lockedNodes.contains(node)

  /**
   * lock the entire model (all nodes)
   *
   * @param locked
   */
  override fun lock(locked: Boolean) {
    log.trace("lock:{}", locked)
    this.locked = locked
  }

  /**
   * @return whether this LayoutModel is locked for all nodes
   */
  override val isLocked: Boolean
    get() = locked

  /**
   * When a visualization is resized, it presumably wants to fix the locations of the nodes and
   * possibly to reinitialize its data. The current method calls `initializeLocations`
   * followed by `initialize_local`.
   */
  override fun setSize(width: Int, height: Int) {
    if (width == 0 || height == 0) {
      throw IllegalArgumentException("Can't be zeros $width/$height")
    }
    val oldWidth = this._width
    val oldHeight = this._height

    if (oldWidth == width && oldHeight == height) {
      return
    }

    if (oldWidth != 0 || oldHeight != 0) {
      adjustLocations(oldWidth, oldHeight, width, height)
    }
    this._width = width
    this._height = height
  }

  /**
   * mode all the nodes to the new center of the layout domain
   *
   * @param oldWidth
   * @param oldHeight
   * @param width
   * @param height
   */
  private fun adjustLocations(oldWidth: Int, oldHeight: Int, width: Int, height: Int) {
    if (oldWidth == width && oldHeight == height) {
      return
    }

    val xOffset = (width - oldWidth) / 2
    val yOffset = (height - oldHeight) / 2

    // now, move each node to be at the new screen center
    while (true) {
      try {
        for (node in graph.nodes()) {
          offsetnode(node, xOffset.toDouble(), yOffset.toDouble())
        }
        break
      } catch (cme: ConcurrentModificationException) {
      }
    }
  }

  /**
   * @return the width of the layout domain
   */
  override val width: Int
    get() = _width

  /**
   * @return the height of the layout domain
   */
  override val height: Int
    get() = _height

  override fun set(node: N, x: Double, y: Double) {
    this.set(node, Point.of(x, y))
  }

  override fun set(node: N, location: Point) {
    if (isFireEvents) {
      fireLayoutChanged(node, location)
    }
  }

  /**
   * @param node the node whose coordinates are to be offset
   * @param xOffset the change to apply to this node's x coordinate
   * @param yOffset the change to apply to this node's y coordinate
   */
  protected fun offsetnode(node: N, xOffset: Double, yOffset: Double) {
    if (!locked && !isLocked(node)) {
      val p = get(node)
      this.set(node, p.x + xOffset, p.y + yOffset)
    }
  }

  override var isFireEvents: Boolean
    get() = changeSupport.isFireEvents
    set(value) {
      changeSupport.isFireEvents = value
    }

  override fun addChangeListener(l: LayoutModel.ChangeListener) {
    changeSupport.addChangeListener(l)
  }

  override fun addLayoutChangeListener(listener: LayoutChangeListener<N>) {
    layoutChangeListeners.add(listener)
  }

  override fun removeLayoutChangeListener(listener: LayoutChangeListener<N>) {
    layoutChangeListeners.remove(listener)
  }

  protected fun fireLayoutChanged(node: N, location: Point) {
    if (layoutChangeListeners.isNotEmpty()) {
      val layoutEvent = LayoutEvent(node, location)
      for (layoutChangeListener in layoutChangeListeners) {
        layoutChangeListener.layoutChanged(layoutEvent)
      }
    }
  }

  override fun removeChangeListener(l: LayoutModel.ChangeListener) {
    changeSupport.removeChangeListener(l)
  }

  override val isRelaxing: Boolean
    get() = _relaxing

  override fun setRelaxing(relaxing: Boolean) {
    log.trace("setRelaxing:{}", relaxing)
    this._relaxing = relaxing
  }

  override fun fireChanged() {
    log.trace("fireChanged")
    changeSupport.fireChanged()
  }

  override val changeListeners: Collection<LayoutModel.ChangeListener>
    get() = changeSupport.changeListeners

  override fun toString(): String =
    "AbstractLayoutModel{hashCode=${hashCode()}, width=$_width, height=$_height, graph of size =${graph.nodes().size}}"

  companion object {
    private val log = LoggerFactory.getLogger(AbstractLayoutModel::class.java)
  }
}
