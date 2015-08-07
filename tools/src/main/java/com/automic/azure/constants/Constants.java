package com.automic.azure.constants;

/**
 * Class contains all the constants used in Docker java application.
 * 
 */
public final class Constants {   
    
    /**
     * https string constant
     */
    public static final String HTTPS = "https";
    
    /**
     * "Unknown Error" string constant
     */
    public static final String UNKNOWN_ERROR = "Unknown Error";
    
    /**
     * int constant for IO Buffer used to buffer the data.
     */
    public static final int IO_BUFFER_SIZE = 4 * 1024;
    
    public static final String AZURE_BASE_URL = "https://management.core.windows.net";
    
    public static final String X_MS_VERSION = "2012-02-21";

    private Constants() {
    }

}
