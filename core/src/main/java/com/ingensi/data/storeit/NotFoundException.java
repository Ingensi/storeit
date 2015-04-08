/*
 * Copyright (c) 2015 Ingensi, Cyres group.
 *
 * See the LICENSE file for copying permission.
 */

package com.ingensi.data.storeit;

/**
 * Specific {@link StorageException} that should be thrown when a requested entity is not found.
 */
public class NotFoundException extends StorageException {
    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
