package com.automic.azure.actions;

/**
 * 
 */


import static com.automic.azure.utility.CommonUtil.print;
import static com.automic.azure.utility.CommonUtil.readFileFromPath;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class StartRoleAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(StartRoleAction.class);

    /*A value that uniquely identifies a request made against the management service*/
    private static final String REQUEST_TOKENID_KEY = "x-ms-request-id";
    
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
    protected void initialize(Map<String,String>args) throws AzureException {
    	serviceName = args.get("ser");
    	deploymentName = args.get("dep");
    	roleName = args.get("rol");
       
    }

    @Override
    protected void validateInputs() throws AzureException {
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
    protected ClientResponse executeSpecific(Client client) throws AzureException{

         ClientResponse response =  null;      
         
         //https://management.core.windows.net/<subscription-id>/services/hostedservices/<service-name>/deployments/<deployment-name>/roleinstances/<role-name>/Operations
         String url = azureUrl+"/%s/services/hostedservices/%s/deployments/%s/roleinstances/%s/Operations";
         url = String.format(url, subscriptionId, serviceName,deploymentName, roleName);
         WebResource webResource = client.resource(url);
         print("Calling url " + webResource.getURI());

         response = webResource.entity(getDescriptor().getBytes(), MediaType.APPLICATION_XML).header(X_MS_VERSION, X_MS_VERSION_VAL).post(ClientResponse.class);

         return response;
    }


    @Override
    protected String getErrorMessage(int errorCode) {

        String msg = null;
        switch (errorCode) {
        case HttpStatus.SC_NOT_FOUND:
            msg = "no such container";
            break;
        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            msg = "server error ";
            break;
         default:
            // msg = Constants.UNKNOWN_ERROR;
             break;
        }
        return msg;
    }

    /**
     * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)}
     * it will print  request token id.
     * 
     */
    @Override
    protected void prepareOutput(ClientResponse response)throws AzureException {   
    	
    	if(response != null){
    		LOGGER.info(" Returned request token id :"+response.getHeaders().get(REQUEST_TOKENID_KEY));
    	}
    	
    	 print(response.getStatus());
         print("TOKEN ID : "+response.getHeaders().get(REQUEST_TOKENID_KEY));
     
         
    }
    
    private String getDescriptor(){
        
        String requestBodyContent="";
      
    	   try {
			requestBodyContent = readFileFromPath("./resource/startVm.xml", false);
		} catch (IOException e) {
			LOGGER.error(" Exception in Reading [startVm.xml] File :"+e);
		}
         
        return requestBodyContent;
    }    
   
}

