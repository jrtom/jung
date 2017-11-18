package edu.uci.ics.jung.layout.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.util.Caching;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadingCacheLayoutModel<N, P> extends AbstractLayoutModel<N, P>
    implements LayoutModel<N, P>, Caching {

  private static final Logger log = LoggerFactory.getLogger(LoadingCacheLayoutModel.class);

  protected LoadingCache<N, P> locations =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> pointModel.newPoint(0, 0, 0)));

  public LoadingCacheLayoutModel(LoadingCacheLayoutModel<N, P> other) {
    super(other.graph, other.pointModel, other.width, other.height, other.depth);
  }

  public LoadingCacheLayoutModel(LoadingCacheLayoutModel<N, P> other, int width, int height) {
    this(other, width, height, 0);
  }

  public LoadingCacheLayoutModel(
      LoadingCacheLayoutModel<N, P> other, int width, int height, int depth) {
    super(other.graph, other.pointModel, width, height, depth);
  }

  public LoadingCacheLayoutModel(
      Graph<N> graph, PointModel<P> pointModel, int width, int height, Function<N, P> initializer) {
    this(graph, pointModel, width, height, 0, initializer);
  }

  public LoadingCacheLayoutModel(
      Graph<N> graph,
      PointModel<P> pointModel,
      int width,
      int height,
      int depth,
      Function<N, P> initializer) {
    super(graph, pointModel, width, height, depth);
    Function<N, P> chain =
        initializer.andThen(
            p -> pointModel.newPoint(pointModel.getX(p), pointModel.getY(p), pointModel.getZ(p)));
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
  }

  public LoadingCacheLayoutModel(Graph<N> graph, PointModel<P> pointModel, int width, int height) {
    this(graph, pointModel, width, height, 0);
  }

  public LoadingCacheLayoutModel(
      Graph<N> graph, PointModel<P> pointModel, int width, int height, int depth) {
    super(graph, pointModel, width, height, depth);
    this.locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> pointModel.newPoint(0, 0)));
  }

  public void setInitializer(Function<N, P> initializer) {
    Function<N, P> chain =
        initializer.andThen(
            p -> pointModel.newPoint(pointModel.getX(p), pointModel.getY(p), pointModel.getZ(p)));
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
  }

  @Override
  public void setGraph(Graph<N> graph) {
    super.setGraph(graph);
    changeSupport.fireChanged();
  }

  @Override
  public void set(N node, P location) {
    if (!locked) {
      this.locations.put(node, location);
      changeSupport.fireChanged();
    }
  }

  @Override
  public void set(N node, double x, double y, double z) {
    this.set(node, pointModel.newPoint(x, y, z));
  }

  @Override
  public void set(N node, double x, double y) {
    this.set(node, pointModel.newPoint(x, y));
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

  /**
   * @param node
   * @param x
   * @param y
   * @param forceUpdate ignored
   */
  @Override
  public void set(N node, double x, double y, double z, boolean forceUpdate) {
    this.set(node, x, y, z);
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

  @Override
  public void clear() {
    this.locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> pointModel.newPoint(0, 0, 0)));
  }
}
