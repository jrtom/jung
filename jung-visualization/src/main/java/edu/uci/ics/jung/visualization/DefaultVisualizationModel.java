/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.awt.Dimension;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The model containing state values for visualizations of graphs. Refactored and extracted from the
 * 1.6.0 version of VisualizationViewer
 *
 * @author Tom Nelson
 */
public class DefaultVisualizationModel implements VisualizationModel, ChangeEventSupport {

  Logger log = LoggerFactory.getLogger(DefaultVisualizationModel.class);

  ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

  /** manages the thread that applies the current layout algorithm */
  protected Relaxer relaxer;

  protected LayoutMediator layoutMediator;

  /** listens for changes in the layout, forwards to the viewer */
  protected ChangeListener changeListener;

  /** @param layout The Layout to apply, with its associated Network */
  public DefaultVisualizationModel(Network network, Layout layout) {
    this(network, layout, null);
  }

  /**
   * Create an instance with the specified layout and dimension.
   *
   * @param layout the layout to use
   * @param d The preferred size of the View that will display this graph
   */
  public DefaultVisualizationModel(Network network, Layout layout, Dimension d) {
    if (changeListener == null) {
      changeListener =
          new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
              fireStateChanged();
            }
          };
    }
    setLayoutMediator(new LayoutMediator(network, layout), d);
  }

  /**
   * Removes the current graph layout, and adds a new one.
   *
   * @param layoutMediator the new layoutMediator to use
   * @param viewSize the size of the View that will display this layout
   */
  public void setLayoutMediator(LayoutMediator layoutMediator, Dimension viewSize) {

    Layout currentLayout = this.layoutMediator != null ? this.layoutMediator.getLayout() : null;
    // remove listener from old layout
    if (currentLayout != null && currentLayout instanceof ChangeEventSupport) {
      ((ChangeEventSupport) currentLayout).removeChangeListener(changeListener);
    }
    Layout newLayout = layoutMediator.getLayout();

    // set to new layout
    if (newLayout instanceof ChangeEventSupport) {
      this.layoutMediator = layoutMediator;
    } else {
      newLayout = new ObservableCachingLayout(layoutMediator.getNetwork(), newLayout);
      this.layoutMediator = new LayoutMediator(layoutMediator.getNetwork(), newLayout);
    }

    ((ChangeEventSupport) newLayout).addChangeListener(changeListener);

    if (viewSize == null) {
      viewSize = new Dimension(600, 600);
    }
    Dimension layoutSize = newLayout.getSize();
    // if the layout has NOT been initialized yet, initialize its size
    // now to the size of the VisualizationViewer window
    if (layoutSize == null) {
      newLayout.setSize(viewSize);
    }
    if (relaxer != null) {
      relaxer.stop();
      relaxer = null;
    }
    //        Layout decoratedLayout = (layout instanceof LayoutDecorator)
    //        	? ((LayoutDecorator) layout).getDelegate()
    //        	: layout;
    if (newLayout instanceof IterativeContext) {
      newLayout.initialize();
      if (relaxer == null) {
        relaxer = new VisRunner((IterativeContext) newLayout);
        //            	relaxer = new VisRunner((IterativeContext) decoratedLayout);
        relaxer.prerelax();
        relaxer.relax();
      }
    }
    fireStateChanged();
  }

  public LayoutMediator getLayoutMediator() {
    return layoutMediator;
  }

  /**
   * set the graph Layout and if it is not already initialized, initialize it to the default
   * VisualizationViewer preferred size of 600x600
   */
  public void setLayoutMediator(LayoutMediator layoutMediator) {
    setLayoutMediator(layoutMediator, null);
  }

  /** @return the relaxer */
  public Relaxer getRelaxer() {
    return relaxer;
  }

  /** @param relaxer the relaxer to set */
  public void setRelaxer(VisRunner relaxer) {
    this.relaxer = relaxer;
  }

  /**
   * Adds a <code>ChangeListener</code>.
   *
   * @param l the listener to be added
   */
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }

  /**
   * Removes a ChangeListener.
   *
   * @param l the listener to be removed
   */
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }

  /**
   * Returns an array of all the <code>ChangeListener</code>s added with addChangeListener().
   *
   * @return all of the <code>ChangeListener</code>s added or an empty array if no listeners have
   *     been added
   */
  public ChangeListener[] getChangeListeners() {
    return changeSupport.getChangeListeners();
  }

  /**
   * Notifies all listeners that have registered interest for notification on this event type. The
   * event instance is lazily created. The primary listeners will be views that need to be repainted
   * because of changes in this model instance
   *
   * @see EventListenerList
   */
  public void fireStateChanged() {
    changeSupport.fireStateChanged();
  }
}
