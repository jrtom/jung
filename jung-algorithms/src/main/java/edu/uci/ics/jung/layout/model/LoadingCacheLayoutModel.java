package edu.uci.ics.jung.layout.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.util.Caching;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that uses a lazy cache for node locations (LoadingCache)
 *
 * @param <N> the node type
 * @param <P> the point type
 * @author Tom Nelson
 */
public class LoadingCacheLayoutModel<N, P> extends AbstractLayoutModel<N, P>
    implements LayoutModel<N, P>, Caching {

  private static final Logger log = LoggerFactory.getLogger(LoadingCacheLayoutModel.class);

  protected LoadingCache<N, P> locations =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> pointModel.newPoint(0, 0, 0)));

  /**
   * a builder for LoadingCache instances
   *
   * @param <N> the node type
   * @param <P> the point type
   * @param <T> the type of the superclass of the LayoutModel to be built
   */
  public abstract static class Builder<N, P, T extends LoadingCacheLayoutModel<N, P>> {
    protected Graph<N> graph;
    protected PointModel<P> pointModel;
    protected int width;
    protected int height;
    protected int depth;

    protected LoadingCache<N, P> locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> pointModel.newPoint(0, 0, 0)));

    /**
     * set the Graph to use for the LayoutModel
     *
     * @param graph
     * @return this builder for further use
     */
    public LoadingCacheLayoutModel.Builder<N, P, T> setGraph(Graph<N> graph) {
      this.graph = graph;
      return this;
    }

    /**
     * set the LayoutModel to copy with this builder
     *
     * @param layoutModel
     * @return this builder for further use
     */
    public LoadingCacheLayoutModel.Builder<N, P, T> setLayoutModel(LayoutModel<N, P> layoutModel) {
      this.pointModel = layoutModel.getPointModel();
      this.width = layoutModel.getWidth();
      this.height = layoutModel.getHeight();
      this.depth = layoutModel.getDepth();
      return this;
    }

    /**
     * sets the pointModel to use in the LayoutModel
     *
     * @param pointModel
     * @return this builder for further use
     */
    public LoadingCacheLayoutModel.Builder<N, P, T> setPointModel(PointModel<P> pointModel) {
      this.pointModel = pointModel;
      return this;
    }

    /**
     * sets the size that will be used for the LayoutModel
     *
     * @param width
     * @param height
     * @param depth
     * @return the LayoutModel.Builder being built
     */
    public LoadingCacheLayoutModel.Builder<N, P, T> setSize(int width, int height, int depth) {
      this.width = width;
      this.height = height;
      this.depth = depth;
      return this;
    }

    /**
     * sets the size that will be used for the LayoutModel
     *
     * @param width
     * @param height
     * @return the LayoutModel.Builder being built
     */
    public LoadingCacheLayoutModel.Builder<N, P, T> setSize(int width, int height) {
      this.width = width;
      this.height = height;
      return this;
    }

    /**
     * sets the initializer to use for new nodes
     *
     * @param initializer
     * @return the builder
     */
    public LoadingCacheLayoutModel.Builder<N, P, T> setInitializer(Function<N, P> initializer) {
      Function<N, P> chain =
          initializer.andThen(
              p -> pointModel.newPoint(pointModel.getX(p), pointModel.getY(p), pointModel.getZ(p)));
      this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
      return this;
    }

    /**
     * build an instance of the requested LayoutModel of type T
     *
     * @return
     */
    public abstract T build();
  }

  public static <N, P> Builder<N, P, ?> builder() {
    return new Builder<N, P, LoadingCacheLayoutModel<N, P>>() {
      @Override
      public LoadingCacheLayoutModel<N, P> build() {
        return new LoadingCacheLayoutModel<>(this);
      }
    };
  }

  public static <N, P> LoadingCacheLayoutModel<N, P> from(LoadingCacheLayoutModel<N, P> other) {
    return new LoadingCacheLayoutModel<>(other);
  }

  protected LoadingCacheLayoutModel(LoadingCacheLayoutModel.Builder<N, P, ?> builder) {
    super(builder.graph, builder.pointModel, builder.width, builder.height, builder.depth);
    this.locations = builder.locations;
  }

  private LoadingCacheLayoutModel(LoadingCacheLayoutModel<N, P> other) {
    super(other.graph, other.pointModel, other.width, other.height, other.depth);
  }

  public void setInitializer(Function<N, P> initializer) {
    Function<N, P> chain =
        initializer.andThen(
            p -> pointModel.newPoint(pointModel.getX(p), pointModel.getY(p), pointModel.getZ(p)));
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
  }

  public Map<N, P> getLocations() {
    return locations.asMap();
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
      super.set(node, location); // will fire events
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
