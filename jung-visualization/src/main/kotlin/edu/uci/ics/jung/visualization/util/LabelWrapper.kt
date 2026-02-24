/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.util

import java.util.function.Function

/**
 * A utility to wrap long lines, creating html strings with line breaks at a settable max line
 * length.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 */
open class LabelWrapper @JvmOverloads constructor(
    private val lineLength: Int = 10
) : Function<String?, String?> {

    /** Call 'wrap' to transform the passed String. */
    override fun apply(str: String?): String? = str?.let { wrap(it) }

    /**
     * Line-wrap the passed String as an html string with break Strings inserted.
     */
    private fun wrap(str: String): String {
        val buf = StringBuilder(str)
        var len = lineLength
        while (len < buf.length) {
            val idx = buf.lastIndexOf(" ", len)
            if (idx != -1) {
                buf.replace(idx, idx + 1, BREAKER)
                len = idx + BREAKER.length + lineLength
            } else {
                buf.insert(len, BREAKER)
                len += BREAKER.length + lineLength
            }
        }
        buf.insert(0, "<html>")
        return buf.toString()
    }

    companion object {
        @JvmField
        val BREAKER: String = "<p>"

        @JvmStatic
        fun main(args: Array<String>) {
            val lines = arrayOf(
                "This is a line with many short words that I will break into shorter lines.",
                "thisisalinewithnobreakssowhoknowswhereitwillwrap",
                "short line"
            )
            val w = LabelWrapper(10)
            for (line in lines) {
                System.err.println("from $line to ${w.wrap(line)}")
            }
        }
    }
}
