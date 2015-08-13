/**
 * 
 */
package com.automic.azure.actions;

import static com.automic.azure.utility.CommonUtil.print;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;


import com.automic.azure.config.HttpClientConfig;
import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.cli.AzureOptions;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.modal.AzureErrorResponse;
import com.automic.azure.utility.CommonUtil;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * 
 * 
 * An abstract action which defines common flow of processes to interact with Azure API. Provides
 * default implementation to initialize arguments, validate parameters, prepare API response and
 * exception handling.
 * 
 */
public abstract class AbstractAction {

  private static final Logger LOGGER = LogManager.getLogger(AbstractAction.class);

  private static final int BEGIN_HTTP_CODE = 200;
  private static final int END_HTTP_CODE = 300;

  /**
   * keystore path
   */
  private String keyStore;

  /**
   * Keystore password
   */

  private String password;

  /**
   * Connection timeout in milliseconds
   */
  private int connectionTimeOut;

  /**
   * Read timeout in milliseconds
   */
  private int readTimeOut;

  /**
   * x-ms-Version
   * */
  protected String x_ms_version;

  private final Options actionOptions = AzureOptions.getAzureOptions();

  private CommandLine cmd;

  public AbstractAction() {
    addOption(Constants.READ_TIMEOUT, true, "Read timeout");
    addOption(Constants.CONNECTION_TIMEOUT, true, "connection timeout");
    addOption(Constants.KEYSTORE_LOCATION, true, "Keystore location");
    addOption(Constants.PASSWORD, true, "Keystore password");
    addOption(Constants.X_MS_VERSION_OPT, true, "x-ms-version");
    addOption(Constants.HELP, false, "show help.");
  }

  protected String getOptionValue(String arg) {
    return cmd.getOptionValue(arg);
  }

  /**
   * Method to trim parameters
   * 
   * @param args
   */
  protected void trim(String[] args) {
    for (int i = 0; i < args.length; i++) {
      args[i] = args[i].trim();
    }
  }

  /**
   * This method acts as template and decides how an action should proceed.It starts with
   * initializing compulsory Options followed by action Options Initializations then logging of
   * parameters ,then checking the number of arguments,then initialize the variables like Azure URL,
   * read and connection timeouts and filepath.Then it will call the REST API of Azure and gets the
   * response which then validated and at last prepares the out either in the form of xml or just a
   * simple sysout.
   * 
   * @param argumentMap
   *          Array of arguments
   * @throws AzureException
   *           exception while executing an action
   */
  public final void executeAction(String[] commandLineArgs) throws AzureException {
    Client client = null;
    try {
      
      trim(commandLineArgs);
      cmd = CommonUtil.getCommandLine(actionOptions, commandLineArgs);
      
      logParameters();
      initializeArguments();
      validateInputs();
      client = getClient();
      ClientResponse response = executeSpecific(client);
      validateResponse(response);
      prepareOutput(response);
    } finally {
      if (client != null) {
        client.destroy();
      }
    }
  }

  /**
   * This function initializes the options for a given action and add to the actionOptions variable.
   * Following are the details needed to create an Option ,short-name e.g act ,isRequired ,have
   * argument/arguments,long-option name and description of Option
   **/
  public final void addOption(String optionName, boolean isRequired, String description) {
    actionOptions.addOption(Option.builder(optionName).required(isRequired).hasArg()
        .desc(description).build());

  }

  /**
   * This function prints the help for a given action
   **/
  protected void help(String action, Options options) {
    HelpFormatter formater = new HelpFormatter();
    formater.printHelp(action, options);

  }

  private void logParameters() {
    LOGGER.info("Input params ");
    for (Option o : cmd.getOptions()) { 
      if(!Constants.PASSWORD.equals(o.getOpt())){
        LOGGER.info( o.getDescription()+"["+o.getOpt()+"]" + " = " + o.getValue());
      }      
    }
  }

  private void initializeArguments() throws AzureException {
      this.connectionTimeOut = CommonUtil.getAndCheckUnsignedValue(cmd.getOptionValue(Constants.CONNECTION_TIMEOUT));
      this.readTimeOut = CommonUtil.getAndCheckUnsignedValue(cmd.getOptionValue(Constants.READ_TIMEOUT));
    this.keyStore = cmd.getOptionValue(Constants.KEYSTORE_LOCATION);
    this.password = cmd.getOptionValue(Constants.PASSWORD);
    this.x_ms_version = cmd.getOptionValue(Constants.X_MS_VERSION_OPT);

    validateGeneralInputs();
    initialize();

  }

  /**
   * Method to validate Input parameters
   * 
   * @throws AzureException
   */
  private void validateGeneralInputs() throws AzureException {

    if (this.connectionTimeOut < 0) {
      LOGGER.error(ExceptionConstants.INVALID_CONNECTION_TIMEOUT);
      throw new AzureException(ExceptionConstants.INVALID_CONNECTION_TIMEOUT);
    }

    if (this.readTimeOut < 0) {
      LOGGER.error(ExceptionConstants.INVALID_READ_TIMEOUT);
      throw new AzureException(ExceptionConstants.INVALID_READ_TIMEOUT);
    }

    if (!Validator.checkFileExistsAndIsFile(this.keyStore)) {
      LOGGER.error(ExceptionConstants.INVALID_FILE);
      throw new AzureException(String.format(ExceptionConstants.INVALID_FILE, this.keyStore));
    }

    if (!Validator.checkNotEmpty(password)) {
      LOGGER.error(ExceptionConstants.EMPTY_PASSWORD);
      throw new AzureException(ExceptionConstants.EMPTY_PASSWORD);
    }

    if (!Validator.checkNotEmpty(x_ms_version)) {
      LOGGER.error(ExceptionConstants.EMPTY_X_MS_VERSION);
      throw new AzureException(ExceptionConstants.EMPTY_X_MS_VERSION);
    }

  }

  protected abstract void initialize();

  /**
   * This method is used to validate the inputs to the action. Override this method to validate
   * action specific inputs
   * 
   * @throws AzureException
   */
  protected abstract void validateInputs() throws AzureException;

  /**
   * Method to write action specific logic.
   * 
   * @param client
   *          an instance of {@link Client}
   * @return an instance of {@link ClientResponse}
   * @throws AzureException
   */
  protected abstract ClientResponse executeSpecific(Client client) throws AzureException;

  /**
   * Method to prepare output based on Response of an HTTP request to client.
   * 
   * @param response
   *          an instance of {@link ClientResponse}
   * @throws AzureException
   */
  protected abstract void prepareOutput(ClientResponse response) throws AzureException;

  /**
   * Method to create an instance of {@link Client} using Azure URL, certificate file path,
   * connection timeout and read timeout.
   * 
   * @return an instance of {@link Client}
   * @throws AzureException
   */
  private Client getClient() throws AzureException {
    return HttpClientConfig.getClient(this.keyStore, this.password, connectionTimeOut, readTimeOut);
  }

  /**
   * Method to validate response from a HTTP client Request. If response is not in range of
   * {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_MULTIPLE_CHOICES}, it throws
   * {@link AzureException} else prints response on console.
   * 
   * @param response
   * @throws AzureException
   */
  private void validateResponse(ClientResponse response) throws AzureException {
    LOGGER.info("Response code for action " + response.getStatus());
    if (!(response.getStatus() >= BEGIN_HTTP_CODE && response.getStatus() < END_HTTP_CODE)) {
      throw new AzureException(getHttpErrorMsg(response));
    }
  }

  /**
   * Method to build Azure response from status code and message.
   * 
   * @param status
   *          Status code
   * @param message
   *          Status message
   * @return Azure response code
   */
  private String buildAzureResponse(int status, String message) {
    StringBuilder responseBuilder = new StringBuilder("Azure Response: ");
    responseBuilder.append("StatusCode: [");
    responseBuilder.append(status).append("]");
    if (Validator.checkNotEmpty(message)) {
      responseBuilder.append(" Message: ").append(message);
    }
    return responseBuilder.toString();
  }

  /**
   * Method to get HTTP error message from an intance of {@link ClientResponse}
   * 
   * @param response
   *          an instance of {@link ClientResponse}
   * @return a String of error message
   * @throws AzureException
   */
  private String getHttpErrorMsg(ClientResponse response) throws AzureException {
    AzureErrorResponse error = response.getEntity(AzureErrorResponse.class);
    String errMsg = buildAzureResponse(response.getStatus(), error.getMessage());
    print(errMsg, LOGGER, StandardLevel.ERROR);
    return error.getCode();
  }

}
