/**
 * 
 */
package com.automic.azure.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.config.HttpClientConfig;
import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureErrorResponse;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Abstract class provides common functionalities to Azure Management Service Actions
 *
 */
public abstract class AbstractManagementAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(AbstractManagementAction.class);
    
    protected String restapiVersion;
    
    protected int connectionTimeOut;
    protected int readTimeOut;
    
    protected String subscriptionId;
    protected String keyStore;
    protected String password;

    /**
     * 
     */
    public AbstractManagementAction() {
    	 addOption(Constants.READ_TIMEOUT, true, "Read timeout");
         addOption(Constants.CONNECTION_TIMEOUT, true, "connection timeout");
         addOption(Constants.X_MS_VERSION_OPT, true, "x-ms-version");
         addOption(Constants.SUBSCRIPTION_ID, true, "Subscription ID");
         addOption(Constants.KEYSTORE_LOCATION, true, "Keystore location");
         addOption(Constants.PASSWORD, true, "Keystore password");
    }
   
    /**
     * Method to initialize parameters 
     */
    @Override
    protected void initializeArguments() {
	    this.connectionTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.CONNECTION_TIMEOUT));
	    this.readTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.READ_TIMEOUT));
	    this.restapiVersion = getOptionValue(Constants.X_MS_VERSION_OPT);
        this.subscriptionId = getOptionValue(Constants.SUBSCRIPTION_ID);
        this.keyStore = getOptionValue(Constants.KEYSTORE_LOCATION);
        this.password = getOptionValue(Constants.PASSWORD);
        // call to initialize action specific param
        initializeActionSpecificArgs();
    }
    
    /**
     * Method to validate Input parameters
     * 
     * @throws AzureException
     */
    @Override
    protected void validateInputs() throws AzureException {
    	
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
    	
        if (!Validator.checkNotEmpty(this.subscriptionId)) {
            LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
            throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
        }
        if (!Validator.checkFileExists(this.keyStore)) {
            LOGGER.error(ExceptionConstants.INVALID_FILE);
            throw new AzureException(String.format(ExceptionConstants.INVALID_FILE, this.keyStore));
        }

        if (!Validator.checkNotEmpty(this.password)) {
            LOGGER.error(ExceptionConstants.EMPTY_PASSWORD);
            throw new AzureException(ExceptionConstants.EMPTY_PASSWORD);
        }
        // validate action specific action
        validateActionSpecificInputs();
    }
    
   
    /**
     * Initializes the HttpClient with sslcontext using keystore and password
     */
    @Override
    protected Client initHttpClient() throws AzureException {
        return HttpClientConfig.getClient(this.keyStore, this.password, connectionTimeOut, readTimeOut);
    }

    @Override
    protected void validateResponse(ClientResponse response) throws AzureException {
        LOGGER.info("Response code for action " + response.getStatus());
        if (!(response.getStatus() >= BEGIN_HTTP_CODE && response.getStatus() < END_HTTP_CODE)) {
            AzureErrorResponse error = response.getEntity(AzureErrorResponse.class);
            throw new AzureException(error.toString());
        }
    }  

    /**
     * Method to initialize Action specific arguments
     */
    protected abstract void initializeActionSpecificArgs();

    /**
     * This method is used to validate the inputs to the action. Override this method to validate action specific inputs
     * 
     * @throws AzureException
     */
    protected abstract void validateActionSpecificInputs() throws AzureException;

}
