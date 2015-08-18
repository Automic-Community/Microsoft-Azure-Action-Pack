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
 * @author sumitsamson
 *
 */
public class DeleteDeploymentAction extends AbstractAction {

	 private static final Logger LOGGER = LogManager.getLogger(DeleteDeploymentAction.class);

	    private String subscriptionId;
	    private String serviceName;
	    private String deploymentName;
	    private boolean deleteMedia;
	    
	    
	    public DeleteDeploymentAction() {
	    	addOption("subscriptionid", true, "Subscription ID");
	        addOption("servicename", true, "Azure cloud service name");
	        addOption("deploymentname", true, "Azure cloud deployment name");	       
	        addOption("deleteMedia", true, "Delete the media(operating system disk, attached data disks & the source blobs)");
	    }
	    
	@Override
	protected void initialize() {        
        serviceName = getOptionValue("servicename");
        deploymentName = getOptionValue("deploymentname");
        subscriptionId = getOptionValue("subscriptionid");
        deleteMedia = CommonUtil.convert2Bool(getOptionValue("deleteMedia"));

	}

	
	@Override
	protected void validateInputs() throws AzureException {
		 if (!Validator.checkNotEmpty(subscriptionId)) {
	            LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
	            throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
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

	
	@Override
	protected ClientResponse executeSpecific(Client client) throws AzureException {
		 ClientResponse response = null;
	        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
	                .path("hostedservices").path(serviceName).path("deployments").path(deploymentName);
	               
	        if (deleteMedia) {
	        	webResource = webResource.queryParam("comp", "media");
	        }
	        
	        LOGGER.info("Calling url " + webResource.getURI());
	        response = webResource.header(Constants.X_MS_VERSION, restapiVersion)
	        		              .accept(MediaType.APPLICATION_XML)
	        		              .delete(ClientResponse.class);
	        return response;
	}

	
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		 List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
	        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));

	}

}
