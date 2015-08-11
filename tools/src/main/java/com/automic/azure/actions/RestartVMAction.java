package com.automic.azure.actions;

/**
 * 
 */

import static com.automic.azure.utility.CommonUtil.print;
import static com.automic.azure.utility.CommonUtil.readFileFromPath;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.modal.RestartVM;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to export the existing container as zip/tar file.It creates the zip/tar at the specified valid location.
 * It will throw error if container id does not exists or file path is invalid
 */
public class RestartVMAction extends AbstractAction {

  
	private static final Logger LOGGER = LogManager.getLogger(RestartVMAction.class);

    private static final String SERVICE_OPT = "servicename";
    private static final String SERVICE_DESC = "Azure cloud service name";    
    private static final String DEPLOYMENT_OPT = "deploymentname";
    private static final String DEPLOYMENT_DESC = "Azure cloud deployment  name";    
    private static final String ROLE_OPT = "rolename";
    private static final String ROLE_DESC = "Role name (VM name)";    
    private String serviceName;
    private String deploymentName;
    private String roleName;
    
    @Override
    protected void logParameters(Map<String,String> args) {

    	LOGGER.info("Input parameters -->");
        LOGGER.info("Connection Timeout = " + args.get(Constants.CONNECTION_TIMEOUT));
        LOGGER.info("Read-timeout = " + args.get(Constants.READ_TIMEOUT));       
        LOGGER.info("Certificate-path = " + args.get(Constants.KEYSTORE_LOCATION));
        LOGGER.info("Certificate-path = " + args.get(Constants.SUBSCRIPTION_ID));
        LOGGER.info("Cloud service name  = " + args.get(SERVICE_OPT));
        LOGGER.info("Deployment name = " + args.get(DEPLOYMENT_OPT));
        LOGGER.info("Role name/ Vm Name = " + args.get(ROLE_OPT));
       

    }

    @Override
	protected Options initializeOptions() {	
    	
		 actionOptions.addOption(Option.builder(SERVICE_OPT).required(true).hasArg().desc(SERVICE_DESC).build());
		 actionOptions.addOption(Option.builder(DEPLOYMENT_OPT).required(true).hasArg().desc(DEPLOYMENT_DESC).build());
		 actionOptions.addOption(Option.builder(ROLE_OPT).required(true).hasArg().desc(ROLE_DESC).build());
		return actionOptions;
	}
    
    @Override
	protected void initialize(Map<String, String> argumentMap) {    	
		serviceName = argumentMap.get(SERVICE_OPT);
    	deploymentName =argumentMap.get(DEPLOYMENT_OPT);
    	roleName = argumentMap.get(ROLE_OPT);
	}

    @Override
    protected void validateInputs(Map<String, String> argumentMap) throws AzureException {
    	
        if (!Validator.checkNotEmpty(serviceName)) {
            LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
        }
        
        if (!Validator.checkNotEmpty(deploymentName)) {
            LOGGER.error(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
        }
        
        if (!Validator.checkNotEmpty(roleName)) {
            LOGGER.error(ExceptionConstants.EMPTY_ROLE_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_ROLE_NAME);
        }        

    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws AzureException {
         ClientResponse response = null;        
         WebResource webResource = client.resource(Constants.AZURE_BASE_URL).path(subscriptionId).path(Constants.SERVICES_PATH).path(Constants.HOSTEDSERVICES_PATH).path(serviceName)
 				.path(Constants.DEPLOYMENTS_PATH).path(deploymentName).path(Constants.ROLEINSTANCES_PATH).path(roleName).path(Constants.OPERATIONS_PATH);
         print("Calling url " + webResource.getURI(), LOGGER, StandardLevel.INFO);
         response = webResource.entity(new RestartVM(), MediaType.APPLICATION_XML).header(Constants.X_MS_VERSION,Constants.X_MS_VERSION_VALUE).post(ClientResponse.class);
         return response;
    }    

    /**
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)}
     * it will print  request token id.
     * 
     */
    @Override
    protected void prepareOutput(ClientResponse response)throws AzureException {   
    		List<String> tokenid=  response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);		
    		print("UC4RB_AZR_REQUEST_ID  ::="+ tokenid.get(0), LOGGER, StandardLevel.INFO);	
    }  
   
}

