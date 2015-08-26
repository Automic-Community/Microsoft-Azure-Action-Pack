/**
 * 
 */
package com.automic.azure.actions;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.cli.AzureCli;
import com.automic.azure.cli.AzureOptions;
import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * An abstract action which defines common flow of processes to interact with Azure API. Provides default implementation
 * to initialize arguments, validate parameters, prepare API response and exception handling.
 */
public abstract class AbstractAction {

    protected static final int BEGIN_HTTP_CODE = 200;
    protected static final int END_HTTP_CODE = 300;

    private static final Logger LOGGER = LogManager.getLogger(AbstractAction.class);

    protected String restapiVersion;
    
    protected int connectionTimeOut;
    protected int readTimeOut;
    private final AzureOptions actionOptions;
    private AzureCli cli;

    public AbstractAction() {
        actionOptions = new AzureOptions();
        addOption(Constants.READ_TIMEOUT, true, "Read timeout");
        addOption(Constants.CONNECTION_TIMEOUT, true, "connection timeout");
        addOption(Constants.X_MS_VERSION_OPT, true, "x-ms-version");
    }

    protected String getOptionValue(String arg) {
        return cli.getOptionValue(arg);
    }

    /**
     * This method acts as template and decides how an action should proceed.It starts with initializing compulsory
     * Options followed by action Options Initializations then logging of parameters ,then checking the number of
     * arguments,then initialize the variables like Azure URL, read and connection timeouts and filepath.Then it will
     * call the REST API of Azure and gets the response which then validated and at last prepares the out either in the
     * form of xml or just a simple sysout.
     * 
     * @param argumentMap
     *            Array of arguments
     * @throws AzureException
     *             exception while executing an action
     */
    public final void executeAction(String[] commandLineArgs) throws AzureException {
        Client client = null;
        try {
            cli = new AzureCli(actionOptions, commandLineArgs);
            cli.log(Arrays.asList(new String[] { Constants.PASSWORD }));
            initializeArguments();
            validateInputs();
            client = initHttpClient();
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
     * This function initializes the options for a given action and add to the actionOptions variable. Following are the
     * details needed to create an Option ,short-name e.g act ,isRequired ,have argument/arguments,long-option name and
     * description of Option
     **/
    public final void addOption(String optionName, boolean isRequired, String description) {
        actionOptions.addOption(optionName, isRequired, description);
    }

    private void initializeArguments() throws AzureException {
        this.connectionTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.CONNECTION_TIMEOUT));
        this.readTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.READ_TIMEOUT));
        this.restapiVersion = getOptionValue(Constants.X_MS_VERSION_OPT);

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

        if (!Validator.checkNotEmpty(restapiVersion)) {
            LOGGER.error(ExceptionConstants.EMPTY_X_MS_VERSION);
            throw new AzureException(ExceptionConstants.EMPTY_X_MS_VERSION);
        }
    }

    /**
     * Method to initialize parameters 
     */
    protected abstract void initialize();

    /**
     * This method is used to validate the inputs to the action. Override this method to validate action specific inputs
     * 
     * @throws AzureException
     */
    protected abstract void validateInputs() throws AzureException;
    
    
    /**
     * Method to initialize Http client 
     * @return
     */
    protected abstract Client initHttpClient() throws AzureException;

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
     * Method to validate response from a HTTP client Request. If response is not in range of 2XX, it throws
     * {@link AzureException} else prints response on console. This Method maps the error response from REST api to
     * {@link AzureErrorResponse}. If your error response xml format is different or differs in namespace
     * "http://schemas.microsoft.com/windowsazure" then override this method.
     * 
     * @param response
     * @throws AzureException
     */
    protected abstract void validateResponse(ClientResponse response) throws AzureException ;

}
