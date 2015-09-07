package com.automic.azure.actions;

import java.io.File;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.ConsoleWriter;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class will create a cloud service in Azure
 * 
 * 
 */
public final class CreateCloudServiceAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(CreateCloudServiceAction.class);

    private String configFilePath;

    public CreateCloudServiceAction() {
        addOption("configfilepath", true, "xml configration file path to create cloud service");
    }

    /**
    * Method to make a call to Azure Management API to create a new cloud service in Azure. 
    * To create a new cloud service, we make a POST
    * https://management.core.windows.net/<subscription-id>/services/hostedservices
    */
    @Override
    public void executeSpecific(Client client) throws AzureException {
        initialize();
        validate();
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
                .path("hostedservices");
        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.entity(new File(configFilePath), MediaType.APPLICATION_XML)
                .header(Constants.X_MS_VERSION, restapiVersion).post(ClientResponse.class);
        prepareOutput(response);
    }

    private void initialize() {
        configFilePath = getOptionValue("configFilePath");
    }

    private void validate() throws AzureException {
        if (!Validator.checkFileExists(configFilePath)) {
            String errMsg = String.format(ExceptionConstants.INVALID_FILE, configFilePath);
            LOGGER.error(errMsg);
            throw new AzureException(errMsg);
        }
    }

    private void prepareOutput(ClientResponse response) throws AzureException {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));
    }

}
