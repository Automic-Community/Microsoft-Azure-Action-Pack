package com.automic.azure.actions;

/**
 * 
 */

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
 * The class will creates a deployment * and then creates a Virtual Machine in the deployment based on the specified
 * configuration
 * 
 * @author Anurag Upadhyay
 */
public class CreateVirtualMachineDeploymentAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(CreateVirtualMachineDeploymentAction.class);

    private String serviceName;
    private String configFilePath;

    /**
     * Initializes a newly created {@code CreateVirtualMachineAction} object.
     */
    public CreateVirtualMachineDeploymentAction() {
        addOption("servicename", true, "Azure cloud service name");
        addOption("configfilepath", true, "Xml configration file path");
    }

    /**
     * Method to make a call to Azure Management API. To create Virtual machine deployment we make a post request to
     * https://management.core.windows.net/<subscription-id>/services/hostedservices/<cloudservice-name>/deployments
     * 
     */
    @Override
    public void executeSpecific(Client client) throws AzureException {
        initialize();
        validate();
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
                .path("hostedservices").path(serviceName).path("deployments");
        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.entity(new File(configFilePath), MediaType.APPLICATION_XML)
                .header(Constants.X_MS_VERSION, restapiVersion).post(ClientResponse.class);

        prepareOutput(response);
    }

    private void initialize() {
        serviceName = getOptionValue("servicename");
        configFilePath = getOptionValue("configfilepath");
    }

    private void validate() throws AzureException {
        if (!Validator.checkNotEmpty(serviceName)) {
            LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
        }
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
