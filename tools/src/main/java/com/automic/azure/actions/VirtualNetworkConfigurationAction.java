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

public class VirtualNetworkConfigurationAction extends AbstractManagementAction {

	 private static final Logger LOGGER = LogManager.getLogger(VirtualNetworkConfigurationAction.class);

	    private String subscriptionId;
	  
	    private String configFilePath;

	    /**
	     * Initializes a newly created {@code VirtualNetworkConfigurationAction} object.
	     */
	    public VirtualNetworkConfigurationAction() {
	        addOption("subscriptionid", true, "Subscription ID");
	        addOption("configfilepath", true, "Xml configration file path");
	    }

	    /**
	     * Method to make a call to Azure Management API. To Set Virtual Network Configuration we make a put request to
	     * https://management.core.windows.net/<subscription-id>/services/networking/media
	     * 
	     */
	    @Override
	    public void executeSpecific(Client client) throws AzureException {
	        initialize();
	        validate();
	        ClientResponse response = null;
	        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId).path("services")
	                .path("networking").path("media");
	        LOGGER.info("Calling url " + webResource.getURI());
	        response = webResource.entity(new File(configFilePath), MediaType.TEXT_PLAIN)
	                .header(Constants.X_MS_VERSION, restapiVersion).put(ClientResponse.class);

	        prepareOutput(response);
	    }

	    private void initialize() {
	        subscriptionId = getOptionValue("subscriptionid");	       
	        configFilePath = getOptionValue("configfilepath");
	    }

	    private void validate() throws AzureException {
	    	
	        if (!Validator.checkNotEmpty(subscriptionId)) {
	            LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
	            throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
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
