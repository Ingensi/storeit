/*
 * Copyright (c) 2015 Ingensi, Cyres group.
 *
 * See the LICENSE file for copying permission.
 */

package com.ingensi.data.storeit;

/**
 * Specific {@link StorageException} that should be thrown when an error occurred from backend (i.e. when external
 * library client throws a specific, not fully managed exception).
 */
public class InternalStorageException extends StorageException {
    public InternalStorageException() {
    }

    public InternalStorageException(String message) {
        super(message);
    }

    public InternalStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalStorageException(Throwable cause) {
        super(cause);
    }

    public InternalStorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
