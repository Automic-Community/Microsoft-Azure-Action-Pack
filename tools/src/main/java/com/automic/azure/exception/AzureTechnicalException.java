/**
 * 
 */
package com.automic.azure.exception;

/**
 * @author sumitsamson
 * 
 */
public class AzureTechnicalException extends RuntimeException {

    
    public AzureTechnicalException() {
        super();

    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public AzureTechnicalException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

    }

    /**
     * @param message
     * @param cause
     */
    public AzureTechnicalException(String message, Throwable cause) {
        super(message, cause);

    }

    /**
     * @param message
     */
    public AzureTechnicalException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public AzureTechnicalException(Throwable cause) {
        super(cause);

    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 8908847435912088181L;

}
