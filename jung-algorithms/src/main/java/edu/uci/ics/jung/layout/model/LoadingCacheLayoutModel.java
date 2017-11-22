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

  public static class Builder<N, P> extends AbstractLayoutModel.Builder<N, P> {
    protected LoadingCache<N, P> locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> pointModel.newPoint(0, 0, 0)));

    public LoadingCacheLayoutModel.Builder<N, P> setGraph(Graph<N> graph) {
      super.setGraph(graph);
      return this;
    }

    public LoadingCacheLayoutModel.Builder<N, P> setPointModel(PointModel<P> pointModel) {
      super.setPointModel(pointModel);
      return this;
    }

    public LoadingCacheLayoutModel.Builder<N, P> setSize(int width, int height, int depth) {
      super.setSize(width, height, depth);
      return this;
    }

    public LoadingCacheLayoutModel.Builder<N, P> setSize(int width, int height) {
      super.setSize(width, height, 0);
      return this;
    }

    public LoadingCacheLayoutModel.Builder<N, P> setInitializer(Function<N, P> initializer) {

      Function<N, P> chain =
          initializer.andThen(
              p -> pointModel.newPoint(pointModel.getX(p), pointModel.getY(p), pointModel.getZ(p)));
      this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
      return this;
    }

    public LoadingCacheLayoutModel<N, P> build() {
      return new LoadingCacheLayoutModel<>(this);
    }

    public LoadingCacheLayoutModel<N, P> from(LoadingCacheLayoutModel<N, P> other) {
      return new LoadingCacheLayoutModel<N, P>(other);
    }
  }

  protected LoadingCacheLayoutModel(LoadingCacheLayoutModel.Builder<N, P> builder) {
    super(builder.graph, builder.pointModel, builder.width, builder.height, builder.depth);
    this.locations = builder.locations;
  }

  LoadingCacheLayoutModel(LoadingCacheLayoutModel<N, P> other) {
    super(other.graph, other.pointModel, other.width, other.height, other.depth);
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
      super.set(node, location);
      this.locations.put(node, location);
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
