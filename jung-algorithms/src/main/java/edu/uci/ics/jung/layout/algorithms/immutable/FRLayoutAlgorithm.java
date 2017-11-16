package edu.uci.ics.jung.layout.algorithms.immutable;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.AbstractIterativeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
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

  public FRLayoutAlgorithm(PointModel<P> pointModel) {
    super(pointModel);
    this.frNodeData =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<N, P>() {
                  public P load(N node) {
                    return pointModel.newPoint(0, 0);
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
    double deltaLength = Math.max(EPSILON, pointModel.distance(fvd));

    double positionX = pointModel.getX(xyd);
    double positionY = pointModel.getY(xyd);
    double newXDisp = pointModel.getX(fvd) / deltaLength * Math.min(deltaLength, temperature);
    double newYDisp = pointModel.getY(fvd) / deltaLength * Math.min(deltaLength, temperature);

    positionX += newXDisp;
    positionY += newYDisp;

    double borderWidth = layoutModel.getWidth() / 50.0;

    if (positionX < borderWidth) {
      positionX = borderWidth + Math.random() * borderWidth * 2.0;
    } else if (positionX > layoutModel.getWidth() - borderWidth * 2) {
      positionX = layoutModel.getWidth() - borderWidth - Math.random() * borderWidth * 2.0;
    }

    if (positionY < borderWidth) {
      positionY = borderWidth + Math.random() * borderWidth * 2.0;
    } else if (positionY > layoutModel.getWidth() - borderWidth * 2) {
      positionY = layoutModel.getWidth() - borderWidth - Math.random() * borderWidth * 2.0;
    }

    layoutModel.set(node, positionX, positionY);
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
    double xDelta = pointModel.getX(p1) - pointModel.getX(p2);
    double yDelta = pointModel.getY(p1) - pointModel.getY(p2);

    double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

    double force = (deltaLength * deltaLength) / attraction_constant;

    Preconditions.checkState(
        !Double.isNaN(force), "Unexpected mathematical result in FRLayout:calcPositions [force]");

    double dx = (xDelta / deltaLength) * force;
    double dy = (yDelta / deltaLength) * force;
    if (v1_locked == false) {
      P fvd1 = getFRData(node1);
      double newX = pointModel.getX(fvd1) - dx;
      double newY = pointModel.getY(fvd1) - dy;
      frNodeData.put(node1, pointModel.newPoint(newX, newY));
    }
    if (v2_locked == false) {
      P fvd2 = getFRData(node2);
      double newX = pointModel.getX(fvd2) + dx;
      double newY = pointModel.getY(fvd2) + dy;
      frNodeData.put(node2, pointModel.newPoint(newX, newY));
    }
  }

  protected void calcRepulsion(N node1) {
    P fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    frNodeData.put(node1, pointModel.newPoint(0, 0));

    try {
      for (N node2 : layoutModel.getGraph().nodes()) {

        //                        if (layoutModel.isLocked(node2)) continue;
        if (node1 != node2) {
          fvd1 = getFRData(node1);
          P p1 = layoutModel.apply(node1);
          P p2 = layoutModel.apply(node2);
          if (p1 == null || p2 == null) {
            continue;
          }
          double xDelta = pointModel.getX(p1) - pointModel.getX(p2);
          double yDelta = pointModel.getY(p1) - pointModel.getY(p2);

          double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

          double force = (repulsion_constant * repulsion_constant) / deltaLength;

          if (Double.isNaN(force)) {
            throw new RuntimeException(
                "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
          }

          double newX = pointModel.getX(fvd1) + (xDelta / deltaLength) * force;
          double newY = pointModel.getY(fvd1) + (yDelta / deltaLength) * force;
          frNodeData.put(node1, pointModel.newPoint(newX, newY));
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
