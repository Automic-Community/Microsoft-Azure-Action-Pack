/**
 * 
 */
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
 * @author sumitsamson
 * 
 */
/**
 * The Create Deployment asynchronous operation uploads a new service package and creates a new deployment in the
 * staging or production environments.
 */
public class CreateDeploymentAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(CreateDeploymentAction.class);

    private String cloudServiceName;
    private String deploymentSlot;
    private String paramterFile;

    public CreateDeploymentAction() {
        addOption("cloudservicename", true, "Name of the cloud service name in which you want to create a deployment");
        addOption("deploymentslot", true, "Target deployment slot[staging or production]");
        addOption("parameterfile", true, "Path of the XML formatted parameter file");

    }

    private void initialize() {
        cloudServiceName = getOptionValue("cloudservicename");
        deploymentSlot = getOptionValue("deploymentslot");
        paramterFile = getOptionValue("parameterfile");
    }

    private void validate() throws AzureException {

        if (!Validator.checkNotEmpty(cloudServiceName)) {
            LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
        }

        if (!Validator.checkNotEmpty(deploymentSlot)) {
            LOGGER.error(ExceptionConstants.EMPTY_DEPLOYMENT_SLOT);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
        }

        if (!Validator.checkFileExists(paramterFile)) {
            String errMsg = String.format(ExceptionConstants.INVALID_FILE, paramterFile);
            LOGGER.error(errMsg);
            throw new AzureException(errMsg);
        }
    }

    /**
     * The Create Deployment request is specified as follows. Replace <subscription-id> with subscription ID,
     * <cloudservice-name> with the name of the cloud service, and <deployment-slot> with staging or production.
     * https://management.core.windows.net/<subscription-id>/services/hostedservices/<cloudservice-name>
     * /deploymentslots/<deployment-slot>
     */
    @Override
    protected void executeSpecific(Client client) throws AzureException {

        initialize();
        validate();
        ClientResponse response = null;
        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
                .path("hostedservices").path(cloudServiceName).path("deploymentslots").path(deploymentSlot);
        LOGGER.info("Calling url " + webResource.getURI());
        response = webResource.entity(new File(paramterFile), MediaType.APPLICATION_XML)
                .header(Constants.X_MS_VERSION, restapiVersion).post(ClientResponse.class);

        prepareOutput(response);

    }

    private void prepareOutput(ClientResponse response) throws AzureException {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));
    }

}
