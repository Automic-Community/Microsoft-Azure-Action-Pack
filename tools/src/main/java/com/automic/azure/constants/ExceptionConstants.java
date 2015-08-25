package com.automic.azure.constants;

/**
 * Constant class containing messages to describe Exception Scenarios.
 * 
 */
public final class ExceptionConstants {

    // General Errors
    public static final String INVALID_ACTION = "Action cannot be empty or null. ";
    public static final String INSUFFICIENT_ARGUMENTS = "Insufficient number of arguments.";
    public static final String GENERIC_ERROR_MSG = "System Error occured.";

    // URL/Http Errors
    public static final String INVALID_AZURE_URL = "Invalid azure url [%s]";
    public static final String NON_INTEGER_TIMEOUT = "Non-integer value for connection timeout or Read timeout";
    public static final String INVALID_CONNECTION_TIMEOUT = "Connection timeout should be positive value";
    public static final String INVALID_READ_TIMEOUT = "Read timeout should be positive value";
    public static final String INVALID_LIMIT_PARAMETER = "Invalid value [%s]. Limit should be positive value";
    
    public static final String INVALID_KEYSTORE = "Invalid KeyStore.";
    public static final String SSLCONTEXT_ERROR = "Unable to build secured context.";

    public static final String INVALID_FILE = " File [%s] is invalid. Possibly file does not exist ";
    public static final String INVALID_DIRECTORY = " Directory [%s] is invalid ";
    public static final String UNABLE_TO_READ_FILE = "Unable to read file [%s]";
    public static final String UNABLE_TO_WRITEFILE = "Error writing file ";
    public static final String UNABLE_TO_WRITE_FILE = "Error writing file [%s]";
    public static final String UNABLE_TO_CREATE_FILE = "Error while creating file";
    public static final String UNABLE_TO_CLOSE_STREAM = "Error while closing stream";
    public static final String UNABLE_TO_FLUSH_STREAM = "Error while flushing stream";
    public static final String UNABLE_TO_READ_INPUTSTREAM = "Unable to read inputstream";
    public static final String UNABLE_TO_CLOSE_INPUTSTREAM = "Unable to close inputstream";
    public static final String UNABLE_TO_COPY_DATA = "Error while copy data on file [%s]";
    public static final String FILE_ALREADY_EXISTS = "Invalid file [%s]. Possibly file already exists";
    public static final String DIRECTORY_ALREADY_EXISTS = "Invalid directory [%s]. Possibly directory already exists";
    public static final String IO_ERROR = "Unknown IO Error";
    public static final String MISSING_REQUIRED_PARAM = "[%s] is missing";
    public static final String EMPTY_SUBSCRIPTION_ID = "Subscription id must not be empty";
    public static final String EMPTY_PASSWORD = "Password cannot be empty";
    public static final String OPTION_VALUE_MISSING = "Value for option %s [%s]is missing";
    public static final String INVALID_ARGS = "Improper Args, probable cause : %s";
    public static final String ACTION_MISSING = "Action[-act] missing in the args";
    public static final String AZURE_CERTIFICATE_MISSING = "Azure certificate not available at specified path";
    public static final String EMPTY_SERVICE_NAME = "Service name should not left blank";
    public static final String EMPTY_DEPLOYMENT_NAME = "Deployment name should not left blank";
    public static final String EMPTY_ROLE_NAME = "Role name should not left blank";
    public static final String EMPTY_POSTSHUTDOWN_ACTION = "Post shutdown action should not left blank";
    public static final String EMPTY_VM_OPERATION_ACTION = "VM operation should not left blank";
    public static final String EMPTY_X_MS_VERSION = "x-ms-version cannot be empty";
    public static final String NUMBER_EXCEPTION = "Value should be number for the following args [%s]";
    public static final String INVALID_AZURE_CERTIFICATE_PWD = "Keystore was tampered with, or password was incorrect";
    public static final String INVALID_VMSTATE_COMMAND = "Invalid command [%s] requested for changing VM state. "
            + "Possible commands [%s]";
    public static final String EMPTY_REQUEST_TOKEN_ID = "Request token id should not left blank";
    
    public static final String EMPTY_STORAGE_ACC_NAME = "Storage account name should not be blank";
    public static final String EMPTY_STORAGE_ACCESS_KEY = "Storage account access key should not left blank";
    public static final String EMPTY_STORAGE_CONTAINER_NAME = "Storage container name should not be blank";
    public static final String EMPTY_STORAGE_CONTAINER_ACCESS = "Storage container access should not left blank";
    public static final String ERROR_STORAGE_AUTHENTICATION = "Error in Creating "
            + "Authentication Signature for Storage Service";

    private ExceptionConstants() {
    }

}
