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
 * <p>
 * Class to delete an existing deployment in Azure. There are two ways to delete a deployment in Azure:
 * <li>
 * <ul>
 * by calling api with deployment slot https://management.core.windows.net/{@literal <subscription-id>}
 * /services/hostedservices/<cloud-service-name >/deploymentslots/{@literal <deployment-slot>}
 * </ul>
 * <ul>
 * by calling api with deployment name https://management.core.windows.net/{@literal <subscription-id>}
 * /services/hostedservices/<cloud-service-name >/deployments/{@literal <deployment-name>}
 * </ul>
 * </li>
 * </p>
 */
public class DeleteDeploymentAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(DeleteDeploymentAction.class);

    /**
     * Cloud service name
     */
    private String cloudServiceName;

    /**
     * Deployment Identifier: could be a deployment slot or a deployment name
     */
    private String deploymentIdentifier;

    /**
     * whether you want to delete the operating system disk, attached data disks, and the source blobs for the disks
     * from storage
     */
    private boolean deleteBySlot;

    /**
     * whether to delete
     */
    private boolean deleteMedia;

    public DeleteDeploymentAction() {
        addOption("cloudservicename", true, "Cloud service name");
        addOption("deletebyslot", true, "Deployment delete mode either Slot or Deployemnt name");
        addOption("deploymentidentifier", true,
                "Deployment Identifier could be Production/Staging slot or a deployment name");
        addOption(
                "deletemedia",
                true,
                "whether you want to delete the operating system disk, attached data disks, and the source blobs for the disks from storage");
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
        if (deleteBySlot) {
            webResource = webResource.path("deploymentslots").path(deploymentIdentifier);
        } else {
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
        deleteBySlot = CommonUtil.convert2Bool(getOptionValue("deletebyslot"));
        deploymentIdentifier = getOptionValue("deploymentidentifier");
        deleteMedia = CommonUtil.convert2Bool(getOptionValue("deletemedia"));
    }

    // validate the inputs
    private void validate() throws AzureException {

        if (!Validator.checkNotEmpty(cloudServiceName)) {
            LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
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
}
