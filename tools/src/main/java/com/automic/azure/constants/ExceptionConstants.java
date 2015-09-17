package com.automic.azure.constants;

/**
 * Constant class containing messages to describe Exception Scenarios.
 * 
 */
public final class ExceptionConstants {

	// General Errors
	public static final String GENERIC_ERROR_MSG = "System Error occured.";

	// URL/Http Errors
	public static final String INVALID_CONNECTION_TIMEOUT = "Connection timeout should be positive value";
	public static final String INVALID_READ_TIMEOUT = "Read timeout should be positive value";

	public static final String INVALID_KEYSTORE = "Invalid KeyStore.";
	public static final String SSLCONTEXT_ERROR = "Unable to build secured context.";

	public static final String INVALID_FILE = " File [%s] is invalid. Possibly file does not exist ";

	public static final String UNABLE_TO_WRITEFILE = "Error writing file ";

	public static final String UNABLE_TO_CLOSE_STREAM = "Error while closing stream";
	public static final String UNABLE_TO_FLUSH_STREAM = "Error while flushing stream";
	public static final String UNABLE_TO_COPY_DATA = "Error while copy data on file [%s]";

	public static final String EMPTY_SUBSCRIPTION_ID = "Subscription id must not be empty";
	public static final String EMPTY_PASSWORD = "Password cannot be empty";
	public static final String OPTION_VALUE_MISSING = "Value for option %s [%s]is missing";
	public static final String INVALID_ARGS = "Improper Args. Possible cause : %s";

	public static final String EMPTY_SERVICE_NAME = "Service name should not be empty";
	public static final String EMPTY_DEPLOYMENT_NAME = "Deployment name should not be empty";
	public static final String EMPTY_ROLE_NAME = "Role name should not be empty";
	public static final String EMPTY_POSTSHUTDOWN_ACTION = "Post shutdown action should not be empty";
	public static final String EMPTY_VM_OPERATION_ACTION = "VM operation should not be empty";
	public static final String EMPTY_X_MS_VERSION = "x-ms-version cannot be empty";

	public static final String INVALID_VMSTATE_COMMAND = "Invalid command [%s] requested for changing VM state. "
			+ "Possible commands [%s]";
	public static final String EMPTY_REQUEST_TOKEN_ID = "Request token id should not left empty";

	public static final String EMPTY_STORAGE_ACC_NAME = "Storage account name should not be empty";
	public static final String INVALID_STORAGE_ACC_NAME = "Storage account name length: 3 to 24 characters, numbers "
			+ "and lowercase letters only. Must match [0-9a-z]{3,24}";
	public static final String EMPTY_STORAGE_ACCESS_KEY = "Storage account access key should not be empty";
	public static final String INVALID_STORAGE_CONTAINER_NAME = "Invalid Storage container name length: "
			+ "3 to 63 characters, numbers, lower-case letters and - only. "
			+ "Dash (-) must be immediately preceded and followed by a letter or number";

	public static final String EMPTY_STORAGE_CONTAINER_ACCESS = "Storage container access should not left blank";
	public static final String INVALID_BLOB_NAME = "Invalid Blob name. Should not end with . or / character."
			+ " Should not contain \\ character." + " Max 1024 characters";

	public static final String INVALID_BLOB_FILE = "Blob File does not exists";
	public static final String INVALID_BLOB_CONTENT_TYPE = "Content-Type of blob should be valid";
	public static final String ERROR_BLOB_MAX_SIZE = "Blob size is exceeded.Expected [<=%s] and actual [%s] bytes";
	public static final String ERROR_BLOCK_BLOB_UPLOAD = "Error while uploading blob as a Block blob";
	public static final String ERROR_COMMITING_BLOCK_BLOB = "Error while commiting block blob";
	public static final String ERROR_STORAGE_AUTHENTICATION = "Error in Creating "
			+ "Authentication Signature for Storage Service";

	public static final String EMPTY_DEPLOYMENT_SLOT = "Deployment slot cannot be empty ,possible values [staging/production]";

	private ExceptionConstants() {
	}

}
