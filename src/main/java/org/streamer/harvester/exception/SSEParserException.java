package org.streamer.harvester.exception;

/**
 * Exception used when the SSE parser is wrong (missing id, event or data)
 */
public class SSEParserException extends Exception {

    public SSEParserException() {
        super();
    }

    public SSEParserException(String message) {
        super(message);
    }

    public SSEParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
