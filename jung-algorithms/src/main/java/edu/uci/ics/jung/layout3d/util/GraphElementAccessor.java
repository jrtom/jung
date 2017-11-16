/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout.model.LayoutModel;

/**
 * Interface for coordinate-based selection of graph components.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public interface GraphElementAccessor<N, P> {
  /**
   * Returns a <code>Vertex</code> which is associated with the location <code>(x,y)</code>. This is
   * typically determined with respect to the <code>Vertex</code>'s location as specified by a
   * <code>Layout</code>.
   */
  N getNode(LayoutModel<N, P> layoutModel, P p);

  /**
   * Returns an <code>Edge</code> which is associated with the location <code>(x,y)</code>. This is
   * typically determined with respect to the <code>Edge</code>'s location as specified by a Layout.
   */
  //    E getEdge(Layout<V,E> layout, double x, double y);

}
