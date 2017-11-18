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

import com.google.common.collect.Lists;
import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.LayoutChangeListener;
import edu.uci.ics.jung.layout.util.LayoutEvent;
import edu.uci.ics.jung.layout.util.LayoutEventSupport;
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent;
import edu.uci.ics.jung.visualization.layout.SpatialLayoutModel;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class BaseVisualizationModel<N, E>
    implements VisualizationModel<N, E, Point2D>,
        ChangeEventSupport,
        LayoutEventSupport<N, Point2D>,
        ChangeListener,
        LayoutModel.ChangeListener {

  private static final Logger log = LoggerFactory.getLogger(BaseVisualizationModel.class);

  protected Network<N, E> network;

  protected LayoutModel<N, Point2D> layoutModel;

  protected LayoutAlgorithm<N, Point2D> layoutAlgorithm;

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);
  private List<LayoutChangeListener<N, Point2D>> layoutChangeListeners = Lists.newArrayList();

  public BaseVisualizationModel(VisualizationModel<N, E, Point2D> other) {
    this(other.getNetwork(), other.getLayoutAlgorithm(), null, other.getLayoutSize());
  }

  public BaseVisualizationModel(VisualizationModel<N, E, Point2D> other, Dimension layoutSize) {
    this(other.getNetwork(), other.getLayoutAlgorithm(), null, layoutSize);
  }

  /**
   * Creates an instance for {@code graph} which does not initialize the node locations.
   *
   * @param network the graph on which the layout algorithm is to operate
   */
  public BaseVisualizationModel(
      Network<N, E> network, LayoutAlgorithm<N, Point2D> layoutAlgorithm) {
    this(network, layoutAlgorithm, null, DEFAULT_SIZE);
  }

  public BaseVisualizationModel(
      Network<N, E> network, LayoutAlgorithm<N, Point2D> layoutAlgorithm, Dimension layoutSize) {
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
      LayoutAlgorithm<N, Point2D> layoutAlgorithm,
      Function<N, Point2D> initializer,
      Dimension layoutSize) {
    //    Preconditions.checkNotNull(network);
    //    Preconditions.checkNotNull(layoutAlgorithm);
    //    Preconditions.checkNotNull(layoutSize);
    this.layoutAlgorithm = layoutAlgorithm;
    this.layoutModel =
        new SpatialLayoutModel(network.asGraph(), layoutSize.width, layoutSize.height);
    //        new LoadingCacheLayoutModel<N, E, Point2D>(network.asGraph(), new AWTPointModel(), layoutSize.width, layoutSize.height);
    if (this.layoutModel instanceof LayoutModel.ChangeSupport) {
      ((LayoutModel.ChangeSupport) layoutModel).addChangeListener(this);
    }
    this.network = network;
    if (initializer != null) {
      this.layoutModel.setInitializer(initializer);
    }

    if (layoutAlgorithm != null) {
      this.layoutModel.accept(layoutAlgorithm);
    }
  }

  public BaseVisualizationModel(
      Network<N, E> network,
      LayoutModel<N, Point2D> layoutModel,
      LayoutAlgorithm<N, Point2D> layoutAlgorithm) {
    this.layoutModel = layoutModel;
    if (this.layoutModel instanceof ChangeEventSupport) {
      ((ChangeEventSupport) layoutModel).addChangeListener(this);
    }
    this.network = network;
    //    if (initializer != null) {
    //      this.layoutModel.setInitializer(initializer);
    //    }
    this.layoutModel.accept(layoutAlgorithm);
    this.layoutAlgorithm = layoutAlgorithm;
  }

  public LayoutModel<N, Point2D> getLayoutModel() {
    log.trace("getting a layourModel " + layoutModel);
    return layoutModel;
  }

  public void setLayoutModel(LayoutModel<N, Point2D> layoutModel) {
    // stop any Relaxer threads before abandoning the previous LayoutModel
    if (this.layoutModel != null) {
      this.layoutModel.stopRelaxer();
    }
    this.layoutModel = layoutModel;
  }

  public void setLayoutAlgorithm(LayoutAlgorithm<N, Point2D> layoutAlgorithm) {
    this.layoutAlgorithm = layoutAlgorithm;
    log.trace("setLayoutAlgorithm to " + layoutAlgorithm);
    //    if (layoutModel instanceof Caching) {
    //      ((Caching) layoutModel).clear();
    //    }
    layoutModel.accept(layoutAlgorithm);
  }

  @Override
  public void setLayoutSize(Dimension size) {
    this.layoutModel.setSize(size.width, size.height);
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
    this.network = network;
    this.layoutModel.setGraph(network.asGraph());
    if (forceUpdate && this.layoutAlgorithm != null) {
      layoutModel.accept(this.layoutAlgorithm);
      changeSupport.fireStateChanged();
    }
  }

  @Override
  public Spatial<N> getSpatial() {
    if (layoutModel instanceof SpatialLayoutModel) {
      return ((SpatialLayoutModel) layoutModel).getSpatial();
    }
    return null;
  }

  public LayoutAlgorithm<N, Point2D> getLayoutAlgorithm() {
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
  public void addLayoutChangeListener(LayoutChangeListener<N, Point2D> listener) {
    this.layoutChangeListeners.add(listener);
  }

  @Override
  public void removeLayoutChangeListener(LayoutChangeListener<N, Point2D> listener) {
    this.layoutChangeListeners.remove(listener);
  }

  private void fireLayoutChanged(LayoutEvent<N, Point2D> layoutEvent, Network<N, E> network) {
    if (!layoutChangeListeners.isEmpty()) {
      LayoutEvent<N, Point2D> evt = new LayoutNetworkEvent<N, Point2D>(layoutEvent, network);
      for (LayoutChangeListener<N, Point2D> listener : layoutChangeListeners) {
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
}
