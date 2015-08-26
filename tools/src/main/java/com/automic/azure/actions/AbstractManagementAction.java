/**
 * 
 */
package com.automic.azure.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.config.HttpClientConfig;
import com.automic.azure.constants.Constants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureErrorResponse;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Abstract class provides common functionalities to Azure Management Service Actions
 *
 */
public abstract class AbstractManagementAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(AbstractManagementAction.class);

    protected String subscriptionId;
    protected String keyStore;
    protected String password;

    /**
     * 
     */
    public AbstractManagementAction() {
        addOption(Constants.SUBSCRIPTION_ID, true, "Subscription ID");
        addOption(Constants.KEYSTORE_LOCATION, true, "Keystore location");
        addOption(Constants.PASSWORD, true, "Keystore password");
    }

    @Override
    protected Client initHttpClient() throws AzureException {
        return HttpClientConfig.getClient(this.keyStore, this.password, connectionTimeOut, readTimeOut);
    }

    @Override
    protected void validateResponse(ClientResponse response) throws AzureException {
        LOGGER.info("Response code for action " + response.getStatus());
        if (!(response.getStatus() >= BEGIN_HTTP_CODE && response.getStatus() < END_HTTP_CODE)) {
            AzureErrorResponse error = response.getEntity(AzureErrorResponse.class);
            StringBuilder responseBuilder = new StringBuilder("Azure Response: ");
            responseBuilder.append("Error Code: [");
            responseBuilder.append(error.getCode()).append("]");
            if (Validator.checkNotEmpty(error.getMessage())) {
                responseBuilder.append(" Message: ").append(error.getMessage());
            }
            throw new AzureException(responseBuilder.toString());
        }
    }

}
