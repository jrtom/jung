/*
 * Copyright (c) 2008, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

package edu.uci.ics.jung.io.graphml;

import edu.uci.ics.jung.io.GraphIOException;

import javax.xml.stream.XMLStreamException;

/**
 * Converts an exception to the a GraphIOException.  Runtime exceptions
 * are checked for the cause.  If the cause is an XMLStreamException, it is
 * converted to a GraphIOException.  Otherwise, the RuntimeException is
 * rethrown.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class ExceptionConverter {

    /**
     * Converts an exception to the a GraphIOException.  Runtime exceptions
     * are checked for the cause.  If the cause is an XMLStreamException, it is
     * converted to a GraphReaderException.  Otherwise, the RuntimeException is
     * rethrown.
     *
     * @param e the exception to be converted
     * @throws GraphIOException the converted exception
     */
    static public void convert(Exception e) throws GraphIOException {

        if (e instanceof GraphIOException) {
            throw (GraphIOException) e;
        }

        if (e instanceof RuntimeException) {

            // If the cause was an XMLStreamException, throw a GraphReaderException
            if (e.getCause() instanceof XMLStreamException) {
                throw new GraphIOException(e.getCause());
            }

            throw (RuntimeException) e;
        }

        throw new GraphIOException(e);
    }
}
