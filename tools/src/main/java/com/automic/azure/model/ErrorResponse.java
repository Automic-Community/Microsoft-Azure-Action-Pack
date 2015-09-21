package com.automic.azure.model;

/**
 * This interface represents generic error response and implementation of this interface need to override 
 * toString functionality.
 */

public interface ErrorResponse {
    
    /**
     * Implements toString functionality
     * @return error response
     */
    String toString();

}
