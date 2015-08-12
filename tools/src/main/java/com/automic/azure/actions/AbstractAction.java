/**
 * 
 */
package com.automic.azure.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.cli.AzureOptions;
import com.automic.azure.config.HttpClientConfig;
import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.utility.CommonUtil;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * 
 * 
 * An abstract action which defines common flow of processes to interact with
 * Azure API. Provides default implementation to initialize arguments, validate
 * parameters, prepare API response and exception handling.
 * 
 */
public abstract class AbstractAction {

	private static final Logger LOGGER = LogManager.getLogger(AbstractAction.class);

	private static final int BEGIN_HTTP_CODE = 200;
	private static final int END_HTTP_CODE = 300;
	
	/**
	 *subscription_id
	 * */
	protected String subscriptionId;

	/**
	 * keystore path
	 */
	protected String keyStore;

	/**
	 * Keystore password
	 */

	protected String password;

	/**
	 * Connection timeout in milliseconds
	 */
	private int connectionTimeOut;

	/**
	 * Read timeout in milliseconds
	 */
	private int readTimeOut;
	
	/**
	 * 
	 */
	protected String xmsVersion;

	
	protected  Options actionOptions = AzureOptions.getAzureOptions();
	
	protected  Map<String, String> actionArgsMap = new HashMap<String, String>(10);

	public AbstractAction() {
		
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
	 * This method acts as template and decides how an action should proceed.It
	 * starts with initializing compulsory Options followed by action Options Initializations
	 * then logging of parameters ,then checking the number of
	 * arguments,then initialize the variables like azure URL, read and
	 * connection timeouts and filepath.Then it will call the REST API of azure
	 * and gets the response which then validated and at last prepares the out
	 * either in the form of xml or just a simple sysout.
	 * 
	 * @param argumentMap
	 *            Array of arguments
	 * @throws AzureException
	 *             exception while executing an action
	 */
	public final void executeAction(String[] commandLineArgs) throws AzureException {
		Client client = null;
		try {
			initializeCompulsoryOptions();
			actionArgsMap = CommonUtil.getMapFromCmdLine(initializeOptions(),commandLineArgs);
			logParameters(actionArgsMap);
			initializeArguments(actionArgsMap);
			validateInputs(actionArgsMap);
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
	
	private void initializeCompulsoryOptions(){
		actionOptions.addOption(Option.builder(Constants.READ_TIMEOUT).required(true).hasArg().desc("Read timeout").build());
		actionOptions.addOption(Option.builder(Constants.CONNECTION_TIMEOUT).required(true).hasArg().desc("connection timeout").build());
		actionOptions.addOption(Option.builder(Constants.KEYSTORE_LOCATION).required(true).hasArg().desc("Keystore location").build());
		actionOptions.addOption(Option.builder(Constants.SUBSCRIPTION_ID).required(true).hasArg().desc("subscription id").build());
		actionOptions.addOption(Option.builder(Constants.PASSWORD).required(true).hasArg().desc("Keystore password").build());
		actionOptions.addOption(Option.builder(Constants.OPTION_X_MS_VERSION).required(true).hasArg().desc("xms version").build());
		actionOptions.addOption(Option.builder(Constants.HELP).required(false).desc("show help.").build());
	}
	
    	
	/**
	 * This function initializes the options for a given action and add to the actionOptions variable.
	 * Following are the details needed to create an Option ,short-name e.g act ,isRequired ,have argument/arguments,long-option name
	 * and description of Option
	 **/
	protected abstract  Options initializeOptions();
	
	
	
	/**
	 * This function prints the help for a given action
	 **/
	protected void help(String action, Options options) {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp(action, options);
		
	}

	protected abstract void logParameters(Map<String, String> argumentMap);
	

	private void initializeArguments(Map<String, String> argumentMap) throws AzureException {
		this.connectionTimeOut = Integer.parseInt(argumentMap.get(Constants.CONNECTION_TIMEOUT));
		this.readTimeOut = Integer.parseInt(argumentMap.get(Constants.READ_TIMEOUT));
		this.subscriptionId = argumentMap.get(Constants.SUBSCRIPTION_ID);
		this.keyStore = argumentMap.get(Constants.KEYSTORE_LOCATION);
		this.password = argumentMap.get(Constants.PASSWORD);
		this.xmsVersion = argumentMap.get(Constants.OPTION_X_MS_VERSION);
		 validateGeneralInputs();
	     initialize(argumentMap);

	};
	
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
		
		
	}

	protected abstract void initialize(Map<String, String> argumentMap) ;
	/**
	 * This method is used to validate the inputs to the action. Override this
	 * method to validate action specific inputs
	 * 
	 * @throws AzureException
	 */
	protected abstract void validateInputs(Map<String, String> argumentMap) throws AzureException;

	/**
	 * Method to write action specific logic.
	 * 
	 * @param client
	 *            an instance of {@link Client}
	 * @return an instance of {@link ClientResponse}
	 * @throws AzureException
	 */
	protected abstract ClientResponse executeSpecific(Client client) throws AzureException;

	

	/**
	 * Method to prepare output based on Response of an HTTP request to client.
	 * 
	 * @param response
	 *            an instance of {@link ClientResponse}
	 * @throws AzureException
	 */
	protected abstract void prepareOutput(ClientResponse response) throws AzureException;

	

	/**
	 * Method to create an instance of {@link Client} using azure URL,
	 * certificate file path, connection timeout and read timeout.
	 * 
	 * @return an instance of {@link Client}
	 * @throws AzureException
	 */
	private Client getClient() throws AzureException {
		return HttpClientConfig.getClient(this.keyStore, this.password, connectionTimeOut, readTimeOut);
	}

	/**
	 * Method to validate response from a HTTP client Request. If response is
	 * not in range of {@link HttpStatus#SC_OK} and
	 * {@link HttpStatus#SC_MULTIPLE_CHOICES}, it throws {@link AzureException}
	 * else prints response on console.
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
	 * Method to build azure response from status code and message.
	 * 
	 * @param status
	 *            Status code
	 * @param message
	 *            Status message
	 * @return azure response code
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
	 * Method to get HTTP error message from an intance of
	 * {@link ClientResponse}
	 * 
	 * @param response
	 *            an instance of {@link ClientResponse}
	 * @return a String of error message
	 */
	private String getHttpErrorMsg(ClientResponse response) {
		String msg = response.getEntity(String.class);
		String errMsg = buildAzureResponse(response.getStatus(), msg);
		System.err.println(errMsg);
		LOGGER.error(errMsg);
		return msg;
	}

}
