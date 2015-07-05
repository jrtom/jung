

# Layout Algorithms #

## I already have vertex positions; how do I use them? ##

This is what [StaticLayout](http://code.google.com/p/jung/source/browse/trunk/jung/jung-algorithms/src/main/java/edu/uci/ics/jung/algorithms/layout/StaticLayout.java) is for.

## How to avoid edge crossings? ##

We don't provide layout algorithms that do that directly.  Even checking for edge crossings requires considering all pairs of edges, which is prohibitively expensive.  The force-directed algorithms generally avoid this as an emergent property of the forces in play, but we provide no guarantees in any of the algorithms for general graphs that we provide.

## What layout should I use? ##

This is not a question that we can really usefully answer as posed, as it depends on how your graph is connected, what visual properties you want it to have, how big it is, and whether you need it to update easily in response to changes in the graph.  If you have this question, please specify these things in your question and we'll try to help you.

## How do I speed up a layout? ##

As of this writing (October 2010) JUNG's (iterative, force-directed) layout algorithms are limited primarily, performance-wise, by the data structures that we're using to maintain the positions.  This is a known problem, there are reasonable ways to fix it, and thus far we simply haven't had time to implement them.  If you'd like to help with this, please contact us.

# Visualization #

## How do I change visual properties (color, shape, size, etc.) of vertices and edges? ##

[PluggableRendererContext](http://code.google.com/p/jung/source/browse/trunk/jung/jung-visualization/src/main/java/edu/uci/ics/jung/visualization/PluggableRenderContext.java)  is responsible for these things.  See PluggableRendererDemo (under jung.samples) for examples of how this is used.

## How do I speed up a visualization? ##

The visualization code has the same limitations that the layout does (see "How do I speed up a layout?"), for the same reasons.