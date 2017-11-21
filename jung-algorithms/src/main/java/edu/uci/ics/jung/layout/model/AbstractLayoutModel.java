package edu.uci.ics.jung.layout.model;

import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.util.VisRunnable;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * superclass for LayoutModels. Holds the required attributes for placing graph nodes in a
 * visualization
 *
 * @author Tom Nelson
 * @param <N>
 * @param <P>
 */
public abstract class AbstractLayoutModel<N, P>
    implements LayoutModel<N, P>, LayoutModel.ChangeSupport {

  private static final Logger log = LoggerFactory.getLogger(AbstractLayoutModel.class);

  private Set<N> lockedNodes = Sets.newHashSet();
  protected boolean locked;
  protected int width;
  protected int height;
  protected int depth;
  protected Graph<N> graph;
  protected PointModel<P> pointModel;
  protected VisRunnable visRunnable;
  protected CompletableFuture theFuture;
  protected LayoutModel.ChangeSupport changeSupport = new DefaultLayoutModelChangeSupport();

  public AbstractLayoutModel(Graph<N> graph, PointModel<P> pointModel, int width, int height) {
    this(graph, pointModel, width, height, 0);
  }

  public AbstractLayoutModel(
      Graph<N> graph, PointModel<P> pointModel, int width, int height, int depth) {
    this.graph = graph;
    this.pointModel = pointModel;
    setSize(width, height, depth);
  }

  /** stop any running Relaxer */
  public void stopRelaxer() {
    if (this.visRunnable != null) {
      this.visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }
  }

  /**
   * accept the visit of a LayoutAlgorithm. If it is an IterativeContext, create a VisRunner to run
   * its relaxer in a new Thread. If there is a current VisRunner, stop it first.
   *
   * @param layoutAlgorithm
   */
  @Override
  public void accept(LayoutAlgorithm<N, P> layoutAlgorithm) {
    changeSupport.setFireEvents(true);
    if (this.visRunnable != null) {
      log.trace("stopping {}", visRunnable);
      this.visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }
    if (log.isTraceEnabled()) {
      log.trace("{} will visit {}", layoutAlgorithm, this);
    }
    if (layoutAlgorithm != null) {
      layoutAlgorithm.visit(this);

      if (layoutAlgorithm instanceof IterativeContext) {
        setupVisRunner((IterativeContext) layoutAlgorithm);
      } else if (log.isTraceEnabled()) {
        log.trace("no visRunner for this {}", this);
      }
    }
  }

  /**
   * create and start a new VisRunner for the passed IterativeContext
   *
   * @param iterativeContext
   */
  protected void setupVisRunner(IterativeContext iterativeContext) {
    log.trace("this {} is setting up a visRunnable with {}", this, iterativeContext);
    if (visRunnable != null) {
      visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }

    // prerelax phase
    changeSupport.setFireEvents(false);

    long timeNow = System.currentTimeMillis();
    while (System.currentTimeMillis() - timeNow < 500 && !iterativeContext.done()) {
      iterativeContext.step();
    }

    changeSupport.setFireEvents(true);

    visRunnable = new VisRunnable(iterativeContext);
    theFuture =
        CompletableFuture.runAsync(visRunnable)
            .thenRun(
                () -> {
                  log.trace("We're done");
                  this.fireChanged();
                });
  }

  /** @return the graph */
  @Override
  public Graph<N> getGraph() {
    return graph;
  }

  public void setGraph(Graph<N> graph) {
    this.graph = graph;
  }

  /**
   * set locked state for the provided node
   *
   * @param node the node to affect
   * @param locked to lock or not
   */
  @Override
  public void lock(N node, boolean locked) {
    if (locked) {
      this.lockedNodes.add(node);
    } else {
      this.lockedNodes.remove(node);
    }
  }

  /**
   * @param node the node whose locked state is being queried
   * @return whether the passed node is locked
   */
  @Override
  public boolean isLocked(N node) {
    return this.lockedNodes.contains(node);
  }

  /**
   * lock the entire model (all nodes)
   *
   * @param locked
   */
  @Override
  public void lock(boolean locked) {
    this.locked = locked;
  }

  /** @return whether this LayoutModel is locked for all nodes */
  @Override
  public boolean isLocked() {
    return this.locked;
  }

  public void setSize(int width, int height) {
    this.setSize(width, height, 0);
  }
  /**
   * When a visualization is resized, it presumably wants to fix the locations of the nodes and
   * possibly to reinitialize its data. The current method calls <tt>initializeLocations</tt>
   * followed by <tt>initialize_local</tt>.
   */
  public void setSize(int width, int height, int depth) {
    if (width == 0 || height == 0) {
      throw new IllegalArgumentException("Can't be zeros " + width + "/" + height + "/" + depth);
    }
    int oldWidth = this.width;
    int oldHeight = this.height;
    int oldDepth = this.depth;

    if (oldWidth == width && oldHeight == height && oldDepth == depth) {
      return;
    }

    if (oldWidth != 0 || oldHeight != 0 || oldDepth != 0) {
      adjustLocations(oldWidth, oldHeight, oldDepth, width, height, depth); //, size);
    }
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  /**
   * mode all the nodes to the new center of the layout domain
   *
   * @param oldWidth
   * @param oldHeight
   * @param width
   * @param height
   */
  private void adjustLocations(
      int oldWidth, int oldHeight, int oldDepth, int width, int height, int depth) {
    if (oldWidth == width && oldHeight == height && oldDepth == depth) {
      return;
    }

    int xOffset = (width - oldWidth) / 2;
    int yOffset = (height - oldHeight) / 2;
    int zOffset = (depth - oldDepth) / 2;

    // now, move each node to be at the new screen center
    while (true) {
      try {
        for (N node : this.graph.nodes()) {
          offsetnode(node, xOffset, yOffset, zOffset);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
  }

  /** @return the current PointModel */
  @Override
  public PointModel<P> getPointModel() {
    return pointModel;
  }

  /** @return the width of the layout domain */
  @Override
  public int getWidth() {
    return width;
  }

  /** @return the height of the layout domain */
  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getDepth() {
    return depth;
  }

  @Override
  public void set(N node, double x, double y) {
    this.set(node, x, y, 0);
  }

  @Override
  public void set(N node, P location) {
    if (isFireEvents()) {
      fireChanged();
    }
  }

  @Override
  public void set(N node, double x, double y, double z) {
    if (isFireEvents()) {
      fireChanged();
    }
  }

  /**
   * @param node the node whose coordinates are to be offset
   * @param xOffset the change to apply to this node's x coordinate
   * @param yOffset the change to apply to this node's y coordinate
   */
  protected void offsetnode(N node, double xOffset, double yOffset, double zOffset) {
    if (!locked && !isLocked(node)) {
      P c = get(node);

      pointModel.setLocation(
          c,
          pointModel.getX(c) + xOffset,
          pointModel.getY(c) + yOffset,
          pointModel.getZ(c) + zOffset);
      this.set(node, c);
    }
  }

  @Override
  public boolean isFireEvents() {
    return changeSupport.isFireEvents();
  }

  @Override
  public void setFireEvents(boolean fireEvents) {
    changeSupport.setFireEvents(fireEvents);
  }

  @Override
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }

  @Override
  public void fireChanged() {
    log.trace("fireChanged");
    changeSupport.fireChanged();
  }

  @Override
  public String toString() {
    return "AbstractLayoutModel{"
        + "hashCode="
        + hashCode()
        + ", width="
        + width
        + ", height="
        + height
        + ", depth="
        + depth
        + ", graph of size ="
        + graph.nodes().size()
        + '}';
  }
}
