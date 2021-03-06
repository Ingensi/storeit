/*
 * Copyright (c) 2015 Ingensi, Cyres group.
 *
 * See the LICENSE file for copying permission.
 */

package com.ingensi.data.storeit;

/**
 * Top level exception for {@link Storage}.
 */
public class StorageException extends Exception {
    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
