/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 7, 2003
 *
 */
package edu.uci.ics.jung.visualization;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.util.LayoutChangeListener;
import edu.uci.ics.jung.layout.util.LayoutEvent;
import edu.uci.ics.jung.layout.util.LayoutEventSupport;
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Function;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class BaseVisualizationModel<N, E>
    implements VisualizationModel<N, E>,
        ChangeEventSupport,
        LayoutEventSupport<N>,
        LayoutChangeListener<N>,
        ChangeListener,
        LayoutModel.ChangeListener {

  private static final Logger log = LoggerFactory.getLogger(BaseVisualizationModel.class);

  protected Network<N, E> network;

  protected LayoutModel<N> layoutModel;

  protected LayoutAlgorithm<N> layoutAlgorithm;

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);
  private List<LayoutChangeListener<N>> layoutChangeListeners = Lists.newArrayList();

  public BaseVisualizationModel(VisualizationModel<N, E> other) {
    this(other.getNetwork(), other.getLayoutAlgorithm(), null, other.getLayoutSize());
  }

  public BaseVisualizationModel(VisualizationModel<N, E> other, Dimension layoutSize) {
    this(other.getNetwork(), other.getLayoutAlgorithm(), null, layoutSize);
  }

  /**
   * @param network the network to visualize
   * @param layoutAlgorithm the algorithm to apply
   * @param layoutSize the size of the layout area
   */
  public BaseVisualizationModel(
      Network<N, E> network, LayoutAlgorithm<N> layoutAlgorithm, Dimension layoutSize) {
    this(network, layoutAlgorithm, null, layoutSize);
  }

  /**
   * Creates an instance for {@code graph} which initializes the node locations using {@code
   * initializer} and sets the layoutSize of the layout to {@code layoutSize}.
   *
   * @param network the graph on which the layout algorithm is to operate
   * @param initializer specifies the starting positions of the nodes
   * @param layoutSize the dimensions of the region in which the layout algorithm will place nodes
   */
  public BaseVisualizationModel(
      Network<N, E> network,
      LayoutAlgorithm<N> layoutAlgorithm,
      Function<N, Point> initializer,
      Dimension layoutSize) {
    Preconditions.checkNotNull(network);
    Preconditions.checkNotNull(layoutSize);
    Preconditions.checkArgument(layoutSize.width > 0, "width must be > 0");
    Preconditions.checkArgument(layoutSize.height > 0, "height must be > 0");
    this.layoutAlgorithm = layoutAlgorithm;
    this.layoutModel =
        LoadingCacheLayoutModel.<N>builder()
            .setGraph(network.asGraph())
            .setSize(layoutSize.width, layoutSize.height)
            .setInitializer(
                new RandomLocationTransformer<N>(
                    layoutSize.width, layoutSize.height, System.currentTimeMillis()))
            .build();

    if (this.layoutModel instanceof LayoutModel.ChangeSupport) {
      ((LayoutModel.ChangeSupport) layoutModel).addChangeListener(this);
    }
    if (layoutModel instanceof LayoutEventSupport) {
      ((LayoutEventSupport) layoutModel).addLayoutChangeListener(this);
    }
    this.network = network;
    if (initializer != null) {
      this.layoutModel.setInitializer(initializer);
    }

    this.layoutModel.accept(layoutAlgorithm);
  }

  public BaseVisualizationModel(
      Network<N, E> network, LayoutModel<N> layoutModel, LayoutAlgorithm<N> layoutAlgorithm) {
    Preconditions.checkNotNull(network);
    Preconditions.checkNotNull(layoutModel);
    this.layoutModel = layoutModel;
    if (this.layoutModel instanceof ChangeEventSupport) {
      ((ChangeEventSupport) layoutModel).addChangeListener(this);
    }
    this.network = network;
    this.layoutModel.accept(layoutAlgorithm);
    this.layoutAlgorithm = layoutAlgorithm;
  }

  public LayoutModel<N> getLayoutModel() {
    log.trace("getting a layourModel " + layoutModel);
    return layoutModel;
  }

  public void setLayoutModel(LayoutModel<N> layoutModel) {
    // stop any Relaxer threads before abandoning the previous LayoutModel
    if (this.layoutModel != null) {
      this.layoutModel.stopRelaxer();
    }
    this.layoutModel = layoutModel;
    if (layoutAlgorithm != null) {
      layoutModel.accept(layoutAlgorithm);
    }
  }

  public void setLayoutAlgorithm(LayoutAlgorithm<N> layoutAlgorithm) {
    this.layoutAlgorithm = layoutAlgorithm;
    log.trace("setLayoutAlgorithm to " + layoutAlgorithm);
    layoutModel.accept(layoutAlgorithm);
  }

  /**
   * Returns the current layoutSize of the visualization space, accoring to the last call to
   * resize().
   *
   * @return the current layoutSize of the screen
   */
  public Dimension getLayoutSize() {
    return new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
  }

  public void setNetwork(Network<N, E> network) {
    this.setNetwork(network, true);
  }

  public void setNetwork(Network<N, E> network, boolean forceUpdate) {
    log.trace("setNetwork to n:{} e:{}", network.nodes(), network.edges());
    this.network = network;
    this.layoutModel.setGraph(network.asGraph());
    if (forceUpdate && this.layoutAlgorithm != null) {
      log.trace("will accept {}", layoutAlgorithm);
      layoutModel.accept(this.layoutAlgorithm);
      log.trace("will fire stateChanged");
      changeSupport.fireStateChanged();
      log.trace("fired stateChanged");
    }
  }

  public LayoutAlgorithm<N> getLayoutAlgorithm() {
    return layoutAlgorithm;
  }

  public Network<N, E> getNetwork() {
    return this.network;
  }

  @Override
  public void addChangeListener(ChangeListener l) {
    this.changeSupport.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l) {
    this.changeSupport.removeChangeListener(l);
  }

  @Override
  public ChangeListener[] getChangeListeners() {
    return changeSupport.getChangeListeners();
  }

  @Override
  public void fireStateChanged() {
    this.changeSupport.fireStateChanged();
  }

  @Override
  public void addLayoutChangeListener(LayoutChangeListener<N> listener) {
    this.layoutChangeListeners.add(listener);
  }

  @Override
  public void removeLayoutChangeListener(LayoutChangeListener<N> listener) {
    this.layoutChangeListeners.remove(listener);
  }

  private void fireLayoutChanged(LayoutEvent<N> layoutEvent, Network<N, E> network) {
    if (!layoutChangeListeners.isEmpty()) {
      LayoutEvent<N> evt = new LayoutNetworkEvent<N>(layoutEvent, network);
      for (LayoutChangeListener<N> listener : layoutChangeListeners) {
        listener.layoutChanged(evt);
      }
    }
  }

  /** this is the event from the LayoutModel */
  @Override
  public void changed() {
    this.fireStateChanged();
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    this.fireStateChanged();
  }

  @Override
  public void layoutChanged(LayoutEvent<N> evt) {
    fireLayoutChanged(evt, network);
  }

  @Override
  public void layoutChanged(LayoutNetworkEvent<N> evt) {
    fireLayoutChanged(evt, network);
  }
}
