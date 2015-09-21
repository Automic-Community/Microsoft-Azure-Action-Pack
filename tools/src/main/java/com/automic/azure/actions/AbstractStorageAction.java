/**
 * 
 */
package com.automic.azure.actions;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.config.HttpClientConfig;
import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.filter.GenericResponseFilter;
import com.automic.azure.filter.StorageAuthenticationFilter;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.model.AzureStorageErrorResponse;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Abstract class provides common functionalities to Azure Storage Service Actions
 *
 */
public abstract class AbstractStorageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(AbstractStorageAction.class);

    protected String restapiVersion;
    /**
     * storage acc from account name and access key
     */
    protected AzureStorageAccount storageAccount;

    private int connectionTimeOut;
    private int readTimeOut;


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
    protected List<String> noLogging() {
        return Arrays.asList(new String[] { Constants.ACCESS_KEY });
    }

    /**
     * Retrieve the ClientConfig which can be used to create client
     * @return ClientConfig Object
     */
    protected ClientConfig getConfig() throws AzureException {
        return HttpClientConfig.getClientConfig(this.connectionTimeOut, this.readTimeOut);
    }

    /**
     * Method to execute the action.
     * 
     * @throws AzureException
     */
    @Override
    public final void execute() throws AzureException {
        Client client = null;
        try {
            initialize();
            validate();
            client = Client.create(getConfig());
            client.addFilter(new StorageAuthenticationFilter(storageAccount));
            client.addFilter(new GenericResponseFilter(AzureStorageErrorResponse.class));
            executeSpecific(client);
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }
    
    /**
     * Method to execute the action.
     * @param client represents Jersey Client Object
     * @throws AzureException
     */
    protected abstract void executeSpecific(Client client) throws AzureException;
    
    private void initialize() {
        this.connectionTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.CONNECTION_TIMEOUT));
        this.readTimeOut = CommonUtil.getAndCheckUnsignedValue(getOptionValue(Constants.READ_TIMEOUT));
        this.restapiVersion = getOptionValue(Constants.X_MS_VERSION_OPT);
        this.storageAccount = new AzureStorageAccount(getOptionValue("storage"), getOptionValue("accesskey"));
    }

    private void validate() throws AzureException {
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

        if (!Validator.checkNotEmpty(storageAccount.getAccountName())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
        } else if (!storageAccount.getAccountName().matches("[0-9a-z]{3,24}")) {
            LOGGER.error(ExceptionConstants.INVALID_STORAGE_ACC_NAME);
            throw new AzureException(ExceptionConstants.INVALID_STORAGE_ACC_NAME);
        }

        if (!Validator.checkNotEmpty(storageAccount.getPrimaryAccessKey())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
        }
    }
    
}
