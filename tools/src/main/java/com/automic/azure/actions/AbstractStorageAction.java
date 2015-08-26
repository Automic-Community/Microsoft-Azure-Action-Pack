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
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.model.AzureStorageErrorResponse;
import com.automic.azure.services.AzureStorageAuthenticationService;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Abstract class provides common functionalities to Azure Storage Service Actions
 *
 */
public abstract class AbstractStorageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(AbstractStorageAction.class);

    /**
     * storage acc from account name and access key
     */
    protected AzureStorageAccount storageAccount;

    /**
     * Storage Authentication Service
     */
    protected AzureStorageAuthenticationService authenticationService;

    private boolean isServiceForTable;
    
    /**
     * @param b
     * 
     */
    public AbstractStorageAction(boolean isServiceForTable) {
        super();
        this.isServiceForTable = isServiceForTable;
        addOption(Constants.STORAGE, true, "Storage Account Name");
        addOption(Constants.ACCESS_KEY, true, "Primary Access Key");
    }

    @Override
    protected void validateInputs() throws AzureException {
        // validate storage name
        if (!Validator.checkNotEmpty(storageAccount.getAccountName())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
        }

        // validate storage access key
        if (!Validator.checkNotEmpty(storageAccount.getPrimaryAccessKey())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
        }

        // validate action specific action
        validateActionSpecificInputs();
    }

    

    @Override
    protected void initialize() {
        // storage acc from account name and access key
        this.storageAccount = new AzureStorageAccount(getOptionValue("storage"), getOptionValue("accesskey"));
        // authentication service
        this.authenticationService = new AzureStorageAuthenticationService(storageAccount, isServiceForTable);
        // call to initialize action specific param
        initializeActionSpecificArgs();

    }

    /**
     * Initializes the HttpClient without keystore and password
     */
    @Override
    protected Client initHttpClient() throws AzureException {
        return HttpClientConfig.getClient(this.connectionTimeOut, this.readTimeOut);
    }

    /**
     * Method overrides validateResponse of {@link AbstractAction} as Error response is of a different namespace than
     * the one returned in abstract class
     */
    @Override
    protected void validateResponse(ClientResponse response) throws AzureException {
        LOGGER.info("Response code for action " + response.getStatus());
        if (!(response.getStatus() >= BEGIN_HTTP_CODE && response.getStatus() < END_HTTP_CODE)) {
            AzureStorageErrorResponse error = response.getEntity(AzureStorageErrorResponse.class);
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
