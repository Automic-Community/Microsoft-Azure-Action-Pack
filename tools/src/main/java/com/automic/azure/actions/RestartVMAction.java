package com.automic.azure.actions;

/**
 * 
 */

import static com.automic.azure.utility.CommonUtil.print;
import static com.automic.azure.utility.CommonUtil.readFileFromPath;

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

    private static final String SERVICE_LONG_OPT = "servicename";
    private static final String SERVICE_DESC = "Azure cloud service name";    
    private static final String DEPLOYMENT_LONG_OPT = "deploymentname";
    private static final String DEPLOYMENT_DESC = "Azure cloud deployment  name";    
    private static final String ROLE_LONG_OPT = "rolename";
    private static final String ROLE_DESC = "Role name (VM name)";    
    private String serviceName;
    private String deploymentName;
    private String roleName;
    
    @Override
    protected void logParameters(Map<String,String> args) {

        LOGGER.info("Input parameters -->");
        LOGGER.info("Connection Timeout = " + args.get("cto"));
        LOGGER.info("Read-timeout = " + args.get("rto"));
        LOGGER.info("Azure-url = " + args.get("url"));
        LOGGER.info("Certificate-path = " + args.get("ksl"));
        LOGGER.info("Cloud service name  = " + args.get("ser"));
        LOGGER.info("Deployment name = " + args.get("dep"));
        LOGGER.info("Role name/ Vm Name = " + args.get("rol"));

    }

    @Override
	protected Options initializeOptions() {	
    	
		 actionOptions.addOption(Option.builder(SERVICE_LONG_OPT).required(true).hasArg()/*.longOpt(SERVICE_LONG_OPT)*/.desc(SERVICE_DESC).build());
		 actionOptions.addOption(Option.builder(DEPLOYMENT_LONG_OPT).required(true).hasArg()/*.longOpt(DEPLOYMENT_LONG_OPT)*/.desc(DEPLOYMENT_DESC).build());
		 actionOptions.addOption(Option.builder(ROLE_LONG_OPT).required(true).hasArg()/*.longOpt(ROLE_LONG_OPT)*/.desc(ROLE_DESC).build());
		return actionOptions;
	}
    
    @Override
	protected void initialize(Map<String, String> argumentMap) {
    	
		serviceName = argumentMap.get(SERVICE_LONG_OPT);
    	deploymentName =argumentMap.get(DEPLOYMENT_LONG_OPT);
    	roleName = argumentMap.get(ROLE_LONG_OPT);
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
         String url = Constants.AZURE_BASE_URL+"/%s/services/hostedservices/%s/deployments/%s/roleinstances/%s/Operations";
         url = String.format(url, subscriptionId, serviceName,deploymentName, roleName);
         WebResource webResource = client.resource(url);
         print("Calling url " + webResource.getURI(), LOGGER, StandardLevel.INFO);
         response = webResource.entity(getDescriptor().getBytes(), MediaType.APPLICATION_XML).header(Constants.X_MS_VERSION,Constants.X_MS_VERSION_VALUE).post(ClientResponse.class);
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
    
	private String getDescriptor() throws AzureException {
		String requestBodyContent = "";
		try {
			requestBodyContent = readFileFromPath("./resource/restartRole.xml",	false);
		} catch (IOException e) {
			LOGGER.error(" Exception in Reading [restartRole.xml] File :" + e);
			throw new AzureException(e.getMessage());
		}
		return requestBodyContent;
	}
   
}

