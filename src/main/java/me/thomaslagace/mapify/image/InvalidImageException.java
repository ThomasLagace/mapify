package me.thomaslagace.mapify.image;

import java.io.IOException;

public class InvalidImageException extends IOException {
    /**
     * Creates a new InvalidImageException.
     *
     * @param message The message of the exception.
     */
    public InvalidImageException(String message) {
        super(message);
    }
}
