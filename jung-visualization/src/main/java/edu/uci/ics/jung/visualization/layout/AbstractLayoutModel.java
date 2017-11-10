package edu.uci.ics.jung.visualization.layout;

import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.layout.util.Relaxer;
import edu.uci.ics.jung.visualization.layout.util.VisRunner;
import java.util.ConcurrentModificationException;
import java.util.Set;
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
public abstract class AbstractLayoutModel<N, P> implements LayoutModel<N, P> {

  private static final Logger log = LoggerFactory.getLogger(AbstractLayoutModel.class);

  private Set<N> lockedNodes = Sets.newHashSet();
  protected boolean locked;
  protected int width;
  protected int height;
  protected Graph<N> graph;
  protected DomainModel<P> domainModel;
  protected VisRunner visRunner;

  public AbstractLayoutModel(Graph<N> graph, DomainModel<P> domainModel, int width, int height) {
    this.graph = graph;
    this.domainModel = domainModel;
    setSize(width, height);
  }

  /** @return a reference to the current Relaxer */
  @Override
  public Relaxer getRelaxer() {
    return this.visRunner;
  }

  /** stop any running Relazer */
  public void stopRelaxer() {
    if (this.visRunner != null) {
      this.visRunner.stop();
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
    setFireEvents(true);
    if (this.visRunner != null) {
      log.trace("stopped the old visRunner: {}", this.visRunner);
      this.visRunner.stop();
    }
    if (log.isTraceEnabled()) {
      log.trace("{} will visit {}", layoutAlgorithm, this);
    }
    if (layoutAlgorithm != null) {
      layoutAlgorithm.visit(this);

      if (layoutAlgorithm instanceof IterativeContext) {
        setupVisRunner((IterativeContext) layoutAlgorithm);
      }
    }
  }

  /**
   * create and start a new VisRunner for the passed IterativeContext
   *
   * @param iterativeContext
   */
  protected void setupVisRunner(IterativeContext iterativeContext) {
    log.trace("set up a visRunner: {}", iterativeContext);
    this.visRunner = new VisRunner(iterativeContext);
    this.visRunner.setSleepTime(500);
    this.setFireEvents(false);
    log.trace("prerelax");
    this.visRunner.prerelax();
    this.setFireEvents(true);
    log.trace("relax");
    this.visRunner.relax();
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

  /**
   * When a visualization is resized, it presumably wants to fix the locations of the nodes and
   * possibly to reinitialize its data. The current method calls <tt>initializeLocations</tt>
   * followed by <tt>initialize_local</tt>.
   */
  public void setSize(int width, int height) {
    if (width == 0 || height == 0) {
      throw new IllegalArgumentException("Can't be zeros " + width + "/" + height);
    }
    int oldWidth = this.width;
    int oldHeight = this.height;

    if (oldWidth == width && oldHeight == height) {
      return;
    }

    if (oldWidth != 0 && oldHeight != 0) {
      adjustLocations(oldWidth, oldHeight, width, height); //, size);
    }
    this.width = width;
    this.height = height;
  }

  /**
   * mode all the nodes to the new center of the layout domain
   *
   * @param oldWidth
   * @param oldHeight
   * @param width
   * @param height
   */
  private void adjustLocations(int oldWidth, int oldHeight, int width, int height) {
    if (oldWidth == width && oldHeight == height) {
      return;
    }

    int xOffset = (width - oldWidth) / 2;
    int yOffset = (height - oldHeight) / 2;

    // now, move each node to be at the new screen center
    while (true) {
      try {
        for (N node : this.graph.nodes()) {
          offsetnode(node, xOffset, yOffset);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
  }

  /** @return the current DomainModel */
  @Override
  public DomainModel<P> getDomainModel() {
    return domainModel;
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

  /**
   * @param node the node whose coordinates are to be offset
   * @param xOffset the change to animate to this node's x coordinate
   * @param yOffset the change to animate to this node's y coordinate
   */
  protected void offsetnode(N node, double xOffset, double yOffset) {
    if (!locked && !isLocked(node)) {
      P c = get(node);

      domainModel.setLocation(c, domainModel.getX(c) + xOffset, domainModel.getY(c) + yOffset);
      this.set(node, c);
    }
  }

  @Override
  public String toString() {
    return "AbstractLayoutModel{"
        + "width="
        + width
        + ", height="
        + height
        + ", graph of size ="
        + graph.nodes().size()
        + '}';
  }
}
