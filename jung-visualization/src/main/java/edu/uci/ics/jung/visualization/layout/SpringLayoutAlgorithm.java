package edu.uci.ics.jung.visualization.layout;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.util.ConcurrentModificationException;
import java.util.function.Function;

/**
 * adapted from the JUNG SpringLayoutClass. Refactored to be a visitor to a LayoutModel holding node
 * positions
 *
 * @param <N>
 * @param <P>
 * @author Tom Nelson
 */
public class SpringLayoutAlgorithm<N, P> extends AbstractIterativeLayoutAlgorithm<N, P>
    implements IterativeContext {

  protected double stretch = 0.70;
  protected Function<? super EndpointPair<N>, Integer> lengthFunction;
  protected int repulsion_range_sq = 100 * 100;
  protected double force_multiplier = 1.0 / 3.0;

  protected LoadingCache<N, SpringNodeData> springNodeData =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> new SpringNodeData()));

  public SpringLayoutAlgorithm(DomainModel<P> domainModel) {
    this(domainModel, n -> 30);
  }

  public SpringLayoutAlgorithm(
      DomainModel<P> domainModel, Function<? super EndpointPair<N>, Integer> length_function) {
    super(domainModel);
    this.lengthFunction = length_function;
  }

  /** @return the current value for the stretch parameter */
  public double getStretch() {
    return stretch;
  }

  public void setStretch(double stretch) {
    this.stretch = stretch;
  }

  public int getRepulsionRange() {
    return (int) (Math.sqrt(repulsion_range_sq));
  }

  public void setRepulsionRange(int range) {
    this.repulsion_range_sq = range * range;
  }

  public double getForceMultiplier() {
    return force_multiplier;
  }

  public void setForceMultiplier(double force) {
    this.force_multiplier = force;
  }

  public void initialize() {}

  public void step() {
    Graph<N> graph = layoutModel.getGraph();
    try {
      for (N node : graph.nodes()) {
        SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        svd.dx /= 4;
        svd.dy /= 4;
        svd.edgedx = svd.edgedy = 0;
        svd.repulsiondx = svd.repulsiondy = 0;
      }
    } catch (ConcurrentModificationException cme) {
      step();
    }

    relaxEdges();
    calculateRepulsion();
    moveNodes();
  }

  protected void relaxEdges() {
    Graph<N> graph = layoutModel.getGraph();
    try {
      for (EndpointPair<N> endpoints : layoutModel.getGraph().edges()) {
        N node1 = endpoints.nodeU();
        N node2 = endpoints.nodeV();

        P p1 = this.layoutModel.get(node1);
        P p2 = this.layoutModel.get(node2);
        if (p1 == null || p2 == null) {
          continue;
        }
        double vx = domainModel.getX(p1) - domainModel.getX(p2); //p1.getX() - p2.getX();
        double vy = domainModel.getY(p1) - domainModel.getY(p2); //p1.getY() - p2.getY();
        double len = Math.sqrt(vx * vx + vy * vy);

        double desiredLen = lengthFunction.apply(endpoints);

        // round from zero, if needed [zero would be Bad.].
        len = (len == 0) ? .0001 : len;

        double f = force_multiplier * (desiredLen - len) / len;

        f = f * Math.pow(stretch, (graph.degree(node1) + graph.degree(node2) - 2));

        // the actual movement distance 'dx' is the force multiplied by the
        // distance to go.
        double dx = f * vx;
        double dy = f * vy;
        SpringNodeData v1D, v2D;
        v1D = springNodeData.getUnchecked(node1);
        v2D = springNodeData.getUnchecked(node2);

        v1D.edgedx += dx;
        v1D.edgedy += dy;
        v2D.edgedx += -dx;
        v2D.edgedy += -dy;
      }
    } catch (ConcurrentModificationException cme) {
      relaxEdges();
    }
  }

  protected void calculateRepulsion() {
    Graph<N> graph = layoutModel.getGraph();

    try {
      for (N node : graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue;
        }

        SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        double dx = 0, dy = 0;

        for (N node2 : graph.nodes()) {
          if (node == node2) {
            continue;
          }
          P p = layoutModel.apply(node);
          P p2 = layoutModel.apply(node2);
          if (p == null || p2 == null) {
            continue;
          }
          double vx = domainModel.getX(p) - domainModel.getX(p2); //p.getX() - p2.getX();
          double vy = domainModel.getY(p) - domainModel.getY(p2); //p.getY() - p2.getY();
          double distanceSq = domainModel.distanceSquared(p, p2); //p.distanceSq(p2);
          if (distanceSq == 0) {
            dx += Math.random();
            dy += Math.random();
          } else if (distanceSq < repulsion_range_sq) {
            double factor = 1;
            dx += factor * vx / distanceSq;
            dy += factor * vy / distanceSq;
          }
        }
        double dlen = dx * dx + dy * dy;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += dx / dlen;
          svd.repulsiondy += dy / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }

  protected void moveNodes() {
    Graph<N> graph = layoutModel.getGraph();

    synchronized (layoutModel) {
      try {
        for (N node : graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          SpringNodeData vd = springNodeData.getUnchecked(node);
          if (vd == null) {
            continue;
          }
          P xyd = layoutModel.apply(node);

          vd.dx += vd.repulsiondx + vd.edgedx;
          vd.dy += vd.repulsiondy + vd.edgedy;

          // keeps nodes from moving any faster than 5 per time unit
          domainModel.setLocation(
              xyd,
              domainModel.getX(xyd) + Math.max(-5, Math.min(5, vd.dx)),
              domainModel.getY(xyd) + Math.max(-5, Math.min(5, vd.dy)));

          int width = layoutModel.getWidth();
          int height = layoutModel.getHeight();

          if (domainModel.getX(xyd) < 0) {
            domainModel.setLocation(xyd, 0, domainModel.getY(xyd));
          } else if (domainModel.getX(xyd) > width) {
            domainModel.setLocation(xyd, width, domainModel.getY(xyd));
          }
          if (domainModel.getY(xyd) < 0) {
            domainModel.setLocation(xyd, domainModel.getX(xyd), 0);
          } else if (domainModel.getY(xyd) > height) {
            domainModel.setLocation(xyd, domainModel.getX(xyd), height);
          }
          // after the bounds have been honored above, really set the location
          // in the layout model
          layoutModel.set(node, xyd);
        }
      } catch (ConcurrentModificationException cme) {
        moveNodes();
      }
    }
  }

  protected static class SpringNodeData {
    protected double edgedx;
    protected double edgedy;
    protected double repulsiondx;
    protected double repulsiondy;

    /** movement speed, x */
    protected double dx;

    /** movement speed, y */
    protected double dy;
  }

  /** @return true */
  public boolean isIncremental() {
    return true;
  }

  /** @return false */
  public boolean done() {
    return false;
  }

  /** No effect. */
  public void reset() {}
}
