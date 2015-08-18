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
 * @author Anurag Upadhyay
 */
public class CheckRequestStatusAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(CheckRequestStatusAction.class);

    private String subscriptionId;
    private String requestTokenId;

    /**
     * Initializes a newly created {@code CheckRequestStatusAction} object.
     */
    public CheckRequestStatusAction() {
        addOption("subscriptionid", true, "Subscription ID");
        addOption("requestid", true, "A value that uniquely identifies a request made against the management service");
    }

    @Override
    protected void initialize() {

        subscriptionId = getOptionValue("subscriptionid");
        requestTokenId = getOptionValue("requestid");
    }

    @Override
    protected void validateInputs() throws AzureException {
        if (!Validator.checkNotEmpty(subscriptionId)) {
            LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
            throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
        }
        if (!Validator.checkNotEmpty(requestTokenId)) {
            LOGGER.error(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
            throw new AzureException(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
        }
    }

    /**
     * Method to make a call to Azure Management API. To get subscription information we make a GET
     * https://management.core.windows.net/<subscription-id>/operations/<request-id>
     * 
     */
    @Override
    protected ClientResponse executeSpecific(Client client) throws AzureException {
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("operations")
                .path(requestTokenId);
        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.header(Constants.X_MS_VERSION, restapiVersion).get(ClientResponse.class);
        return response;
    }

    /**
     * This method will print request status.
     * 
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws AzureException {
        AzureRequestStatusModel azReqstatus = response.getEntity(AzureRequestStatusModel.class);
        String status = "UC4RB_AZR_REQUEST_STATUS ::= " + azReqstatus.getRequestStatus();
        if (azReqstatus.getError() != null) {
            AzureErrorResponse azErrorResponse = azReqstatus.getError();
            status += "\nError Code : " + azErrorResponse.getCode();
            status += "\nError Message : " + azErrorResponse.getMessage();
        }
        ConsoleWriter.writeln(status);
    }

}
