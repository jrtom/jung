package edu.uci.ics.jung.visualization.util

/**
 * A class that is used to link together a graph element and a specific graph. Provides appropriate
 * implementations of `hashCode` and `equals`.
 */
class Context<G, E> private constructor(
    /** The graph element which defines this context. */
    val graph: G,
    /** The edge element which defines this context. */
    val element: E
) {

    override fun hashCode(): Int = graph.hashCode() xor element.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is Context<*, *>) return false
        return other.graph == graph && other.element == element
    }

    companion object {
        /**
         * Returns an instance of this type for the specified graph and element.
         *
         * @param G the graph type
         * @param E the element type
         */
        @JvmStatic
        fun <G, E> getInstance(graph: G, element: E): Context<G, E> = Context(graph, element)
    }
}
