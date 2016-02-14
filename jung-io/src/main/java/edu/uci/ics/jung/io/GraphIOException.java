/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io;

/**
 * Exception thrown when IO errors occur when reading/writing graphs.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class GraphIOException extends Exception {

    private static final long serialVersionUID = 3773882099782535606L;

    /**
     * Creates a new instance with no specified message or cause.
     */
    public GraphIOException() {
        super();
    }

    /**
     * Creates a new instance with the specified message and cause.
     * @param message a description of the exception-triggering event
     * @param cause the exception which triggered this one
     */
    public GraphIOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the specified message and no
     * specified cause.
     * @param message a description of the exception-triggering event
     */
    public GraphIOException(String message) {
        super(message);
    }

    /**
     * Creats a new instance with the specified cause and no specified message.
     * @param cause the exception which triggered this one
     */
    public GraphIOException(Throwable cause) {
        super(cause);
    }

}
