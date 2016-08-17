package org.onosproject.oxp.exceptions;

/**
 * Created by cr on 16-4-7.
 */
public class OXPParseError extends Exception {
    private static final long serialVersionUID = 1L;

    public OXPParseError() {
        super();
    }

    public OXPParseError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public OXPParseError(final String message) {
        super(message);
    }

    public OXPParseError(final Throwable cause) {
        super(cause);
    }
}
