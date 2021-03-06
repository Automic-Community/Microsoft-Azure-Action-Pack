/**
 * 
 */
package com.automic.azure.exception;

/**
 * Exception class thrown to indicate that error has occurred while processing request. It could either be <li>
 * <ul>
 * Business exception for invalid inputs to Actions
 * </ul>
 * <ul>
 * Technical exception to denote errors while communicating with Azure API
 * </ul>
 * </li>
 * 
 */
public class AzureException extends Exception {

    private static final long serialVersionUID = -3274150618150755200L;

    /**
     * Constructor that takes an error message
     * 
     * @param message
     */
    public AzureException(String message) {
        super(message);

    }

}
