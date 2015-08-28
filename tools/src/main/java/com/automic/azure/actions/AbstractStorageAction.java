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
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Abstract class provides common functionalities to Azure Storage Service Actions
 *
 */
public abstract class AbstractStorageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(AbstractStorageAction.class);

    protected String restapiVersion;

    protected int connectionTimeOut;
    protected int readTimeOut;

    /**
     * storage acc from account name and access key
     */
    protected AzureStorageAccount storageAccount;

    /**
     * No-args constructor
     */
    public AbstractStorageAction() {
        addOption(Constants.READ_TIMEOUT, true, "Read timeout");
        addOption(Constants.CONNECTION_TIMEOUT, true, "connection timeout");
        addOption(Constants.X_MS_VERSION_OPT, true, "x-ms-version");
        addOption(Constants.STORAGE, true, "Storage Account Name");
        addOption(Constants.ACCESS_KEY, true, "Primary Access Key");
    }

    @Override
    protected void initializeArguments() {

        this.connectionTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.CONNECTION_TIMEOUT));
        this.readTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.READ_TIMEOUT));
        this.restapiVersion = getOptionValue(Constants.X_MS_VERSION_OPT);
        // storage acc from account name and access key
        this.storageAccount = new AzureStorageAccount(getOptionValue("storage"), getOptionValue("accesskey"));
        // call to initialize action specific param
        initializeActionSpecificArgs();

    }

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

        // validate storage name and matches [0-9a-z]{3,24}
        if (!Validator.checkNotEmpty(storageAccount.getAccountName())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
        } else if (!storageAccount.getAccountName().matches("[0-9a-z]{3,24}")) {
            LOGGER.error(ExceptionConstants.INVALID_STORAGE_ACC_NAME);
            throw new AzureException(ExceptionConstants.INVALID_STORAGE_ACC_NAME);
        }

        // validate storage access key
        if (!Validator.checkNotEmpty(storageAccount.getPrimaryAccessKey())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
        }

        // validate action specific action
        validateActionSpecificInputs();

    }

    /**
     * Initializes the HttpClient without keystore and password
     */
    @Override
    protected ClientConfig initHttpClient() throws AzureException {
        return HttpClientConfig.getClientConfig(this.connectionTimeOut, this.readTimeOut);
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
