/*
 * Created on Mar 10, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import com.google.common.base.Preconditions;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import java.util.function.Function;
import javax.swing.Icon;

/**
 * Supplies an Icon for each node according to the <code>Icon</code> parameters given in the
 * constructor, so that picked and non-picked nodes can be made to look different.
 */
public class PickableNodeIconFunction<N> implements Function<N, Icon> {

  protected Icon icon;
  protected Icon picked_icon;
  protected PickedInfo<N> pi;

  // FIXME: we don't need both this and Pickable{Edge,Node}PaintTransformer;
  // just make a generic version that covers all three

  /**
   * @param pi specifies which nodes report as "picked"
   * @param icon <code>Icon</code> used to represent nodes
   * @param picked_icon <code>Icon</code> used to represent picked nodes
   */
  public PickableNodeIconFunction(PickedInfo<N> pi, Icon icon, Icon picked_icon) {
    this.pi = Preconditions.checkNotNull(pi);
    this.icon = Preconditions.checkNotNull(icon);
    this.picked_icon = Preconditions.checkNotNull(picked_icon);
  }

  /** Returns the appropriate <code>Icon</code>, depending on picked state. */
  public Icon apply(N v) {
    return pi.isPicked(v) ? picked_icon : icon;
  }
}
