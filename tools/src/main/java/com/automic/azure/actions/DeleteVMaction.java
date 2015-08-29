/**
 * 
 */
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
 * Action class to delete a Virtual Machine 
 * 
 */
public class DeleteVMaction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(DeleteVMaction.class);

    private String vmName;
    private String serviceName;
    private String deploymentName;
    private boolean deleteMedia;

    public DeleteVMaction() {
        addOption("vmname", true, "Virtual machine name");
        addOption("servicename", true, "Azure cloud service name");
        addOption("deploymentname", true, "Azure cloud deployment name");
        addOption("deletemedia", true,
                "Delete the media(operating system disk, attached data disks & the source blobs)");
    }

    @Override
    protected void initializeSpecific() {
        vmName = getOptionValue("vmname");
        serviceName = getOptionValue("servicename");
        deploymentName = getOptionValue("deploymentname");
        deleteMedia = CommonUtil.convert2Bool(getOptionValue("deletemedia"));

    }

    @Override
    protected void validateSpecific() throws AzureException {
        if (!Validator.checkNotEmpty(vmName)) {
            LOGGER.error(ExceptionConstants.EMPTY_ROLE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_ROLE_NAME);
        }
        if (!Validator.checkNotEmpty(serviceName)) {
            LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
        }
        if (!Validator.checkNotEmpty(deploymentName)) {
            LOGGER.error(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
        }

    }

    /**
     * To delete the VM this method made a call to Azure Rest API https://management
     * .core.windows.net/<subscription-id>/services/hostedservices /<cloudservice-name>/deployments/<deployment-name>
     * /roles/<role-name> The URI parameter "comp=media" is optional and specifies that the operating system disk,
     * attached data disks, and the source blobs for the disks should also be deleted from storage.
     */
    @Override
    protected ClientResponse executeSpecific(Client client) throws AzureException {
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
                .path("hostedservices").path(serviceName).path("deployments").path(deploymentName).path("roles")
                .path(vmName);

        if (deleteMedia) {
            webResource = webResource.queryParam("comp", "media");
        }

        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.header(Constants.X_MS_VERSION, restapiVersion).accept(MediaType.APPLICATION_XML)
                .delete(ClientResponse.class);
        return response;
    }

    /**
     * This method will print the request id on console once all the valid input params are passed.
     **/
    @Override
    protected void prepareOutput(ClientResponse response) throws AzureException {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));

    }

}
