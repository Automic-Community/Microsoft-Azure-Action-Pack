/**
 * 
 */
package com.automic.azure.exception;

/**
 * @author sumitsamson
 * 
 */
public class AzureRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 8908847435912088181L;

    /**
     * @param message
     */
    public AzureRuntimeException(String message) {
        super(message);

    }

}
