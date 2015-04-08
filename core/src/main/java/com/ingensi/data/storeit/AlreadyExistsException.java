/*
 * Copyright (c) 2015 Ingensi, Cyres group.
 *
 * See the LICENSE file for copying permission.
 */

package com.ingensi.data.storeit;

/**
 * Specific {@link StorageException} that should be thrown when an operation is requested on an unexisting ID, but it
 * is found into storage.
 */
public class AlreadyExistsException extends StorageException {
    public AlreadyExistsException() {
    }

    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public AlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
