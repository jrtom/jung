package edu.uci.ics.jung.visualization.layout;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.util.ConcurrentModificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implements the Fruchterman-Reingold force-directed algorithm for node layout. */
public class FRLayoutAlgorithm<N, P> extends AbstractIterativeLayoutAlgorithm<N, P>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRLayoutAlgorithm.class);

  private double forceConstant;

  private double temperature;

  private int currentIteration;

  private int mMaxIterations = 700;

  protected LoadingCache<N, P> frNodeData;

  private double attraction_multiplier = 0.75;

  private double attraction_constant;

  private double repulsion_multiplier = 0.75;

  private double repulsion_constant;

  private double max_dimension;

  private boolean initialized = false;

  public FRLayoutAlgorithm(DomainModel<P> domainModel) {
    super(domainModel);
    this.frNodeData =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<N, P>() {
                  public P load(N node) {
                    return domainModel.newPoint(0, 0);
                  }
                });
  }

  @Override
  public void visit(LayoutModel<N, P> layoutModel) {
    log.trace("visiting " + layoutModel);

    super.visit(layoutModel);
    max_dimension = Math.max(layoutModel.getWidth(), layoutModel.getHeight());
    initialize();
  }

  public void setAttractionMultiplier(double attraction) {
    this.attraction_multiplier = attraction;
  }

  public void setRepulsionMultiplier(double repulsion) {
    this.repulsion_multiplier = repulsion;
  }

  public void reset() {
    doInit();
  }

  public void initialize() {
    doInit();
  }

  private void doInit() {
    Graph<N> graph = layoutModel.getGraph();
    if (graph != null && graph.nodes().size() > 0) {
      currentIteration = 0;
      temperature = layoutModel.getWidth() / 10;

      forceConstant =
          Math.sqrt(layoutModel.getHeight() * layoutModel.getWidth() / graph.nodes().size());

      attraction_constant = attraction_multiplier * forceConstant;
      repulsion_constant = repulsion_multiplier * forceConstant;
      initialized = true;
    }
  }

  private double EPSILON = 0.000001D;

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between nodes and
   * edges and cooling the temperature.
   */
  public synchronized void step() {
    //    log.info("step");
    if (!initialized) {
      doInit();
    }
    Graph<N> graph = layoutModel.getGraph();
    currentIteration++;

    /** Calculate repulsion */
    while (true) {

      try {
        for (N node1 : graph.nodes()) {
          calcRepulsion(node1);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    /** Calculate attraction */
    while (true) {
      try {
        for (EndpointPair<N> endpoints : graph.edges()) {
          calcAttraction(endpoints);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    while (true) {
      try {
        for (N node : graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          calcPositions(node);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    cool();
  }

  protected synchronized void calcPositions(N node) {

    P fvd = getFRData(node);
    if (fvd == null) {
      return;
    }
    P xyd = layoutModel.apply(node);
    double deltaLength = Math.max(EPSILON, domainModel.distance(fvd));

    double newXDisp = domainModel.getX(fvd) / deltaLength * Math.min(deltaLength, temperature);

    Preconditions.checkState(
        !Double.isNaN(newXDisp),
        "Unexpected mathematical result in FRLayout:calcPositions [xdisp]");

    double newYDisp = domainModel.getY(fvd) / deltaLength * Math.min(deltaLength, temperature);
    domainModel.setLocation(
        xyd, domainModel.getX(xyd) + newXDisp, domainModel.getY(xyd) + newYDisp);

    double borderWidth = layoutModel.getWidth() / 50.0;
    double newXPos = domainModel.getX(xyd);
    if (newXPos < borderWidth) {
      newXPos = borderWidth + Math.random() * borderWidth * 2.0;
    } else if (newXPos > (layoutModel.getWidth() - borderWidth)) {
      newXPos = layoutModel.getWidth() - borderWidth - Math.random() * borderWidth * 2.0;
    }

    double newYPos = domainModel.getY(xyd);
    if (newYPos < borderWidth) {
      newYPos = borderWidth + Math.random() * borderWidth * 2.0;
    } else if (newYPos > (layoutModel.getHeight() - borderWidth)) {
      newYPos = layoutModel.getHeight() - borderWidth - Math.random() * borderWidth * 2.0;
    }

    domainModel.setLocation(xyd, newXPos, newYPos);
    layoutModel.set(node, xyd);
  }

  protected void calcAttraction(EndpointPair<N> endpoints) {
    N node1 = endpoints.nodeU();
    N node2 = endpoints.nodeV();
    boolean v1_locked = layoutModel.isLocked(node1);
    boolean v2_locked = layoutModel.isLocked(node2);

    if (v1_locked && v2_locked) {
      // both locked, do nothing
      return;
    }
    P p1 = layoutModel.apply(node1);
    P p2 = layoutModel.apply(node2);
    if (p1 == null || p2 == null) {
      return;
    }
    double xDelta = domainModel.getX(p1) - domainModel.getX(p2);
    double yDelta = domainModel.getY(p1) - domainModel.getY(p2);

    double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

    double force = (deltaLength * deltaLength) / attraction_constant;

    Preconditions.checkState(
        !Double.isNaN(force), "Unexpected mathematical result in FRLayout:calcPositions [force]");

    double dx = (xDelta / deltaLength) * force;
    double dy = (yDelta / deltaLength) * force;
    if (v1_locked == false) {
      P fvd1 = getFRData(node1);
      domainModel.offset(fvd1, -dx, -dy);
    }
    if (v2_locked == false) {
      P fvd2 = getFRData(node2);
      domainModel.offset(fvd2, dx, dy);
    }
  }

  protected void calcRepulsion(N node1) {
    P fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    domainModel.setLocation(fvd1, 0, 0);

    try {
      for (N node2 : layoutModel.getGraph().nodes()) {

        //                        if (layoutModel.isLocked(node2)) continue;
        if (node1 != node2) {
          P p1 = layoutModel.apply(node1);
          P p2 = layoutModel.apply(node2);
          if (p1 == null || p2 == null) {
            continue;
          }
          double xDelta = domainModel.getX(p1) - domainModel.getX(p2);
          double yDelta = domainModel.getY(p1) - domainModel.getY(p2);

          double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

          double force = (repulsion_constant * repulsion_constant) / deltaLength;

          if (Double.isNaN(force)) {
            throw new RuntimeException(
                "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
          }

          domainModel.offset(fvd1, (xDelta / deltaLength) * force, (yDelta / deltaLength) * force);
        }
      }
    } catch (ConcurrentModificationException cme) {
      calcRepulsion(node1);
    }
  }

  private void cool() {
    temperature *= (1.0 - currentIteration / (double) mMaxIterations);
  }

  public void setMaxIterations(int maxIterations) {
    mMaxIterations = maxIterations;
  }

  protected P getFRData(N node) {
    return frNodeData.getUnchecked(node);
  }

  /** @return true */
  public boolean isIncremental() {
    return true;
  }

  /** @return true once the current iteration has passed the maximum count. */
  public boolean done() {
    if (currentIteration > mMaxIterations || temperature < 1.0 / max_dimension) {
      return true;
    }
    return false;
  }
}
