package com.automic.azure.constants;

/**
 * Class contains all the constants used in Azure java application.
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
  public static final String AZURE_MGMT_URL = "https://management.core.windows.net";
  public static final String X_MS_VERSION = "x-ms-version";
  public static final String X_MS_VERSION_OPT = "xmsversion";
  public static final String REQUEST_TOKENID_KEY = "x-ms-request-id";
  public static final String HELP = "help";
  public static final String PASSWORD = "password";
  public static final String KEYSTORE_LOCATION = "keystore";
  public static final String SUBSCRIPTION_ID = "subscriptionid";
  public static final String OPTION_X_MS_VERSION = "xmsversion";
  public static final String CONNECTION_TIMEOUT = "connectiontimeout";
  public static final String READ_TIMEOUT = "readtimeout";
  public static final String ACTION = "action";
  public static final String OPERATIONTYPE_SHUTDOWN = "ShutdownRoleOperation";
  public static final String OPERATIONTYPE_RESTART = "RestartRoleOperation";
  public static final String OPERATIONTYPE_START = "StartRoleOperation";

  private Constants() {
  }

}
