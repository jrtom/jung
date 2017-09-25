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
import org.apache.log4j.Logger;

/**
 * The model containing state values for visualizations of graphs. Refactored and extracted from the
 * 1.6.0 version of VisualizationViewer
 *
 * @author Tom Nelson
 */
public class DefaultVisualizationModel<N, E>
    implements VisualizationModel<N, E>, ChangeEventSupport {

  Logger log = Logger.getLogger(DefaultVisualizationModel.class);

  ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

  /** manages the thread that applies the current layout algorithm */
  protected Relaxer relaxer;

  protected LayoutMediator<N, E> layoutMediator;

  /** listens for changes in the layout, forwards to the viewer */
  protected ChangeListener changeListener;

  /** @param layout The Layout to apply, with its associated Network */
  public DefaultVisualizationModel(Network<N, E> network, Layout<N> layout) {
    this(network, layout, null);
  }

  /**
   * Create an instance with the specified layout and dimension.
   *
   * @param layout the layout to use
   * @param d The preferred size of the View that will display this graph
   */
  public DefaultVisualizationModel(Network<N, E> network, Layout<N> layout, Dimension d) {
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
  public void setLayoutMediator(LayoutMediator<N, E> layoutMediator, Dimension viewSize) {

    log.debug("setLayoutMediator to " + layoutMediator + " with size " + viewSize);
    Layout<N> currentLayout = this.layoutMediator != null ? this.layoutMediator.getLayout() : null;
    log.debug("the currentLayout was " + currentLayout);
    // remove listener from old layout
    if (currentLayout != null && currentLayout instanceof ChangeEventSupport) {
      ((ChangeEventSupport) currentLayout).removeChangeListener(changeListener);
      log.debug("removed its change listener");
    }
    Layout<N> newLayout = layoutMediator.getLayout();
    log.debug("the new layout is " + newLayout);
    log.debug("the new? network is " + layoutMediator.getNetwork());

    // set to new layout
    if (newLayout instanceof ChangeEventSupport) {
      this.layoutMediator = layoutMediator;
      log.debug("already is ChangeEventSupport");
      //      this.layout = layout;
    } else {
      log.debug("made a Observable");
      newLayout = new ObservableCachingLayout<N, E>(layoutMediator.getNetwork(), newLayout);
      this.layoutMediator = new LayoutMediator(layoutMediator.getNetwork(), newLayout);
    }
    log.debug("now the layoutMediator is " + this.layoutMediator);

    ((ChangeEventSupport) newLayout).addChangeListener(changeListener);

    if (viewSize == null) {
      viewSize = new Dimension(600, 600);
    }
    Dimension layoutSize = newLayout.getSize();
    log.debug("layoutSize:" + layoutSize);
    // if the layout has NOT been initialized yet, initialize its size
    // now to the size of the VisualizationViewer window
    if (layoutSize == null) {
      log.debug("set layoutSize to " + viewSize);
      newLayout.setSize(viewSize);
      //      newLayout.setSize(viewSize);
    }
    if (relaxer != null) {
      relaxer.stop();
      relaxer = null;
    }
    //        Layout<N> decoratedLayout = (layout instanceof LayoutDecorator)
    //        	? ((LayoutDecorator<N>) layout).getDelegate()
    //        	: layout;
    if (newLayout instanceof IterativeContext) {
      log.debug(newLayout + " is an IterativeContext");
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

  public LayoutMediator<N, E> getLayoutMediator() {
    return layoutMediator;
  }

  /**
   * set the graph Layout and if it is not already initialized, initialize it to the default
   * VisualizationViewer preferred size of 600x600
   */
  public void setLayoutMediator(LayoutMediator<N, E> layoutMediator) {
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
