package com.automic.azure.actions;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Class to delete an existing deployment in Azure. There are two ways to delete a deployment in Azure
 * 
 */
public class DeleteDeploymentAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(DeleteDeploymentAction.class);

    /**
     * 
     * Enum for different modes for deleting deployment
     *
     */
    private static enum DeleteMode {
        SLOT, NAME
    }

    private String cloudServiceName;
    private String deploymentIdentifier;
    private String deploymentDeleteMode;

    private boolean deleteMedia;

    public DeleteDeploymentAction() {
        addOption("cloudservicename", true, "Cloud service name");
        addOption("deletemode", true, "Deployment delete mode either Slot or Deployemnt name");
        addOption("deploymentidentifier", true,
                "Deployment Identifier could be Production/Staging slot or a deployment name");
        addOption("deletemedia", true,
                "Delete the media(operating system disk, attached data disks & the source blobs)");
    }

    /**
     * <p>
     * Method to make a call to Azure Management API to delete an existing deployment in Azure. To do so, we make a
     * DELETE call either
     * <li>
     * <ul>
     * by deployment slot
     * https://management.core.windows.net/<subscription-id>/services/hostedservices/<cloud-service-name
     * >/deploymentslots/<deployment-slot>
     * </ul>
     * <ul>
     * by deployment name
     * https://management.core.windows.net/<subscription-id>/services/hostedservices/<cloud-service-name
     * >/deployments/<deployment-name>
     * </ul>
     * </li>
     * </p>
     */
    @Override
    public void executeSpecific(Client client) throws AzureException {
        // initialize the parameters
        initialize();
        // validate the parameters
        validate();

        // create request
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
                .path("hostedservices").path(cloudServiceName);

        // check if we delete deployment based on slot or deployment name
        if (DeleteMode.SLOT.toString().equals(deploymentDeleteMode)) {
            webResource = webResource.path("deploymentslots").path(deploymentIdentifier);
        } else if (DeleteMode.NAME.toString().equals(deploymentDeleteMode)) {
            webResource = webResource.path("deployments").path(deploymentIdentifier);
        }

        // whether operating system disk, attached data disks, and the source blobs for the disks should also be deleted
        // from storage.
        if (deleteMedia) {
            webResource = webResource.queryParam("comp", "media");
        }
        LOGGER.info("Calling url " + webResource.getURI());
        // calling the
        response = webResource.header(Constants.X_MS_VERSION, restapiVersion).accept(MediaType.APPLICATION_XML)
                .delete(ClientResponse.class);

        // publish request id
        prepareOutput(response);

    }

    // initialize the parameters
    private void initialize() {
        cloudServiceName = getOptionValue("cloudservicename");
        deploymentDeleteMode = getOptionValue("deletemode");
        deploymentIdentifier = getOptionValue("deploymentidentifier");
        deleteMedia = CommonUtil.convert2Bool(getOptionValue("deletemedia"));
    }

    // validate the inputs
    private void validate() throws AzureException {

        if (!Validator.checkNotEmpty(cloudServiceName)) {
            LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
        }

        if (!validateDeleteMode()) {
            LOGGER.error(ExceptionConstants.INVALID_DEPLYMENT_TYPE_MODE);
            throw new AzureException(ExceptionConstants.INVALID_DEPLYMENT_TYPE_MODE);
        }

        if (!Validator.checkNotEmpty(deploymentIdentifier)) {
            LOGGER.error(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
        }
    }

    // publish the request id
    private void prepareOutput(ClientResponse response) {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));
    }

    // validate delete mode of deployment
    private boolean validateDeleteMode() {
        boolean isValid = false;
        try {
            DeleteMode.valueOf(deploymentDeleteMode);
            isValid = true;
        } catch (IllegalArgumentException ex) {
            
        }

        return isValid;
    }

}
