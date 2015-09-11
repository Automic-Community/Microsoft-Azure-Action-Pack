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
 * Class to delete an existing cloud service in Azure .
 * 
 * @author kamalgarg
 * 
 */
public class DeleteCloudServiceAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(DeleteCloudServiceAction.class);

    private String cloudServiceName;
    private boolean deleteMedia;

    public DeleteCloudServiceAction() {
        addOption("cloudservicename", true, "Name of the cloud service to be deleted");
        addOption("deletemedia", true,
                "Delete the media(operating system disk, attached data disks & the source blobs)");
    }

    /**
     * Method to make a call to Azure Management API to delete an existing cloud service in Azure. To do so, we make a
     * DELETE call https://management.core.windows.net/<subscription-id>/services/hostedservices/<cloud-service-name>
     */
    @Override
    public void executeSpecific(Client client) throws AzureException {
        initialize();
        validate();
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
                .path("hostedservices").path(cloudServiceName);
        if (deleteMedia) {
            webResource = webResource.queryParam("comp", "media");
        }
        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.header(Constants.X_MS_VERSION, restapiVersion).accept(MediaType.APPLICATION_XML)
                .delete(ClientResponse.class);
        prepareOutput(response);

    }

    private void initialize() {
        cloudServiceName = getOptionValue("cloudservicename");
        deleteMedia = CommonUtil.convert2Bool(getOptionValue("deletemedia"));
    }

    private void validate() throws AzureException {
        if (!Validator.checkNotEmpty(cloudServiceName)) {
            LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
        }
    }

    private void prepareOutput(ClientResponse response) {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("Request ID : " + tokenid.get(0));
    }

}
