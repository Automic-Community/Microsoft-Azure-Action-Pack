package com.automic.azure.constants;

/**
 * Class contains all the constants used in Azure java application.
 * 
 */
public final class Constants {


    /**
     * int constant for IO Buffer used to buffer the data.
     */
    public static final int IO_BUFFER_SIZE = 4 * 1024;
    public static final String AZURE_MGMT_URL = "https://management.core.windows.net";
    public static final String X_MS_VERSION = "x-ms-version";
    public static final String X_MS_VERSION_OPT = "xmsversion";
    public static final String REQUEST_TOKENID_KEY = "x-ms-request-id";
    public static final String HELP = "help";
    public static final String PASSWORD = "password";
    public static final String KEYSTORE_LOCATION = "keystore";
    public static final String SUBSCRIPTION_ID = "subscriptionid";
    public static final String STORAGE = "storage";
    public static final String ACCESS_KEY = "accesskey";
    public static final String CONTAINER_NAME = "containername";
    public static final String CONTENT_TYPE = "contenttype";
    public static final String OPTION_X_MS_VERSION = "xmsversion";
    public static final String CONNECTION_TIMEOUT = "connectiontimeout";
    public static final String READ_TIMEOUT = "readtimeout";
    public static final String ACTION = "action";
    public static final String OPERATIONTYPE_SHUTDOWN = "ShutdownRoleOperation";
    public static final String OPERATIONTYPE_RESTART = "RestartRoleOperation";
    public static final String OPERATIONTYPE_START = "StartRoleOperation";
    public static final String AZURE_ERROR_NAMESPACE = "http://schemas.microsoft.com/windowsazure";
    
    public static final String STORAGE_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    private Constants() {
    }

}
