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
import com.automic.azure.model.AzureErrorResponse;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Abstract class provides common functionalities to Azure Management Service Actions
 *
 */
public abstract class AbstractManagementAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(AbstractManagementAction.class);

    protected String restapiVersion;
    protected String subscriptionId;

    private int connectionTimeOut;
    private int readTimeOut;
    private String keyStore;
    private String password;

    public AbstractManagementAction() {
        addOption(Constants.READ_TIMEOUT, true, "Read timeout");
        addOption(Constants.CONNECTION_TIMEOUT, true, "connection timeout");
        addOption(Constants.X_MS_VERSION_OPT, true, "x-ms-version");
        addOption(Constants.SUBSCRIPTION_ID, true, "Subscription ID");
        addOption(Constants.KEYSTORE_LOCATION, true, "Keystore location");
        addOption(Constants.PASSWORD, true, "Keystore password");
    }

    @Override
    protected List<String> noLogging() {
        return Arrays.asList(new String[] { Constants.PASSWORD });
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
            client.addFilter(new GenericResponseFilter(AzureErrorResponse.class));
            executeSpecific(client);
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }
    
    /**
     * Retrieve the ClientConfig which can be used to create client
     * @return ClientConfig Object
     */
    protected ClientConfig getConfig() throws AzureException {
        return HttpClientConfig.getClientConfig(this.keyStore, this.password, connectionTimeOut, readTimeOut);
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
        this.subscriptionId = getOptionValue(Constants.SUBSCRIPTION_ID);
        this.keyStore = getOptionValue(Constants.KEYSTORE_LOCATION);
        this.password = getOptionValue(Constants.PASSWORD);
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
    }

}
