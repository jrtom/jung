/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.util;

import com.google.common.base.Function;

/**
 * A utility to wrap long lines, creating html strings
 * with line breaks at a settable max line length
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public class LabelWrapper implements Function<String,String> {

	int lineLength;
	public static final String breaker = "<p>";
	
	/**
	 * Create an instance with default line break length = 10
	 *
	 */
	public LabelWrapper() {
		this(10);
	}
	
	/**
	 * Create an instance with passed line break length
	 * @param lineLength the max length for lines
	 */
	public LabelWrapper(int lineLength) {
		this.lineLength = lineLength;
	}

	/**
	 * call 'wrap' to transform the passed String
	 */
	public String apply(String str) {
		if(str != null) {
			return wrap(str);
		} else {
			return null;
		}
	}
	
	/**
	 * line-wrap the passed String as an html string with
	 * break Strings inserted.
	 * 
	 * @param str
	 * @return
	 */
	private String wrap(String str) {
		StringBuilder buf = new StringBuilder(str);
		int len = lineLength;
		while(len < buf.length()) {
			int idx = buf.lastIndexOf(" ", len);
			if(idx != -1) {
				buf.replace(idx, idx+1, breaker);
				len = idx + breaker.length() +lineLength;
			} else {
				buf.insert(len, breaker);
				len += breaker.length() + lineLength;
			}
		}
		buf.insert(0, "<html>");
		return buf.toString();
	}
	
	public static void main(String[] args) {
		String[] lines = {
				"This is a line with many short words that I will break into shorter lines.",
				"thisisalinewithnobreakssowhoknowswhereitwillwrap",
				"short line"
		};
		LabelWrapper w = new LabelWrapper(10);
		for(int i=0; i<lines.length; i++) {
			System.err.println("from "+lines[i]+" to "+w.wrap(lines[i]));
		}
	}
}
