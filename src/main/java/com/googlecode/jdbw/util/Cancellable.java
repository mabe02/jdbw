package com.googlecode.jdbw.util;

/**
 * Interface exposing a single cancel() method to allow for cancelling database
 * certain database operations.
 * 
 * @author mberglun
 */
public interface Cancellable {
    /**
     * Attempts to cancel the ongoing operation.
     * 
     * @throws Exception If there was any kind of error while cancelling
     */
    void cancel() throws Exception;
}
