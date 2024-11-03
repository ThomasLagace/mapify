package me.thomaslagace.mapify.image;

import java.io.IOException;

public class DomainNotAllowedException extends IOException {
    /**
     * Creates a new DomainNotAllowedException.
     *
     * @param message The exception message.
     */
    public DomainNotAllowedException(String message) {
        super(message);
    }
}
