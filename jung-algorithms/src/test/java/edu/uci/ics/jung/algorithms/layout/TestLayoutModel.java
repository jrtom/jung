package edu.uci.ics.jung.algorithms.layout;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLayoutModel<N, P> extends AbstractLayoutModel<N, P> implements LayoutModel<N, P> {

  private static final Logger log = LoggerFactory.getLogger(TestLayoutModel.class);

  protected LoadingCache<N, P> locations =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> domainModel.newPoint(0, 0)));

  public TestLayoutModel(AbstractLayoutModel<N, P> other) {
    super(other.graph, other.domainModel, other.width, other.height);
  }

  public TestLayoutModel(AbstractLayoutModel<N, P> other, int width, int height) {
    super(other.graph, other.domainModel, width, height);
  }

  public TestLayoutModel(
      Graph<N> graph,
      DomainModel<P> domainModel,
      int width,
      int height,
      Function<N, P> initializer) {
    super(graph, domainModel, width, height);
    Function<N, P> chain =
        initializer.andThen(
            p -> (P) domainModel.newPoint(domainModel.getX(p), domainModel.getY(p)));
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(initializer::apply));
  }

  public TestLayoutModel(Graph<N> graph, DomainModel<P> domainModel, int width, int height) {
    super(graph, domainModel, width, height);
    this.locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> domainModel.newPoint(0, 0)));
  }

  public void setInitializer(Function<N, P> initializer) {
    Function<N, P> chain =
        initializer.andThen(
            p -> (P) domainModel.newPoint(domainModel.getX(p), domainModel.getY(p)));
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(initializer::apply));
  }

  @Override
  public void setGraph(Graph<N> graph) {
    super.setGraph(graph);
    //    this.locations.invalidateAll();
  }

  @Override
  public void setFireEvents(boolean fireEvents) {
    // no-op for test class
  }

  @Override
  public void set(N node, P location) {
    if (!locked) {
      this.locations.put(node, location);
    }
  }

  @Override
  public void set(N node, double x, double y) {
    this.set(node, domainModel.newPoint(x, y));
  }

  /**
   * @param node
   * @param location
   * @param forceUpdate ignored
   */
  @Override
  public void set(N node, P location, boolean forceUpdate) {
    this.set(node, location);
  }

  /**
   * @param node
   * @param x
   * @param y
   * @param forceUpdate ignored
   */
  @Override
  public void set(N node, double x, double y, boolean forceUpdate) {
    this.set(node, x, y);
  }

  @Override
  public P get(N node) {
    if (log.isTraceEnabled()) {
      log.trace(this.locations.getUnchecked(node) + " gotten for " + node);
    }
    return this.locations.getUnchecked(node);
  }

  @Override
  public P apply(N node) {
    return this.get(node);
  }

  public void clear() {
    this.locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> domainModel.newPoint(0, 0)));
  }
}
