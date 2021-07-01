package com.automic.azure.actions;

/**
 * 
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureErrorResponse;
import com.automic.azure.model.AzureRequestStatusModel;
import com.automic.azure.util.ConsoleWriter;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class will get the status of the specified operation. After calling an asynchronous operation, you can call
 * CheckRequestStatusAction to determine whether the operation has succeeded, failed, or is still in progress.
 * 
 */
public final class CheckRequestStatusAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(CheckRequestStatusAction.class);

    private String requestTokenId;

    /**
     * Initializes a newly created {@code CheckRequestStatusAction} object.
     */
    public CheckRequestStatusAction() {
        addOption("requestid", true, "A value that uniquely identifies a request made against the management service");
    }

    /**
     * Method to make a call to Azure Management API. To get subscription information we make a GET
     * https://management.core.windows.net/<subscription-id>/operations/<request-id>
     * 
     */
    @Override
    public void executeSpecific(Client client) throws AzureException {
        initialize();
        validate();
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("operations")
                .path(requestTokenId);
        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.header(Constants.X_MS_VERSION, restapiVersion).get(ClientResponse.class);
        prepareOutput(response);
    }

    private void initialize() {
        this.requestTokenId = getOptionValue("requestid");
    }

    private void validate() throws AzureException {
        if (!Validator.checkNotEmpty(this.requestTokenId)) {
            LOGGER.error(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
            throw new AzureException(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
        }
    }

    private void prepareOutput(ClientResponse response) throws AzureException {
        AzureRequestStatusModel azReqstatus = response.getEntity(AzureRequestStatusModel.class);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_STATUS ::= " + azReqstatus.getRequestStatus());
        ConsoleWriter.writeln("HTTPStatusCode : " + azReqstatus.getHttpStatusCode());
        if (azReqstatus.getError() != null) {
            AzureErrorResponse azErrorResponse = azReqstatus.getError();
            ConsoleWriter.writeln("Error Code : " + azErrorResponse.getCode());
            ConsoleWriter.writeln("Error Message : " + azErrorResponse.getMessage());
        }
    }

}
