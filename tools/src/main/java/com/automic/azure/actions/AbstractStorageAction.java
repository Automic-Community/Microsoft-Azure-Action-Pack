/**
 * 
 */
package com.automic.azure.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.config.HttpClientConfig;
import com.automic.azure.constants.Constants;
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

    /**
     * 
     */
    public AbstractStorageAction() {
        addOption(Constants.STORAGE, true, "Storage Account Name");
        addOption(Constants.ACCESS_KEY, true, "Primary Access Key");

    }

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
