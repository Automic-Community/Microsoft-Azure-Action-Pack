/**
 * 
 */
package com.automic.azure.actions;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.ContainerAccess;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.services.AzureStorageAuthenticationService;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to create a Container in Azure Storage
 *
 */
public class CreateStorageContainerAction extends AbstractAction {

	
	private static final Logger LOGGER = LogManager.getLogger(CreateStorageContainerAction.class);
	
	/**
	 *  storage acc from account name and access key
	 */
    AzureStorageAccount storageAccount;
	
	/**
	 * Storage Authentication Service
	 */
	private AzureStorageAuthenticationService authenticationService;
	
	/**
	 * 
	 */
	private  String containerName;

	
	/**
	 * 
	 */
	private ContainerAccess containerAccess;
	
	/**
	 * 
	 */
	public CreateStorageContainerAction() {
		addOption("accountname", true, "Storage Account Name");
		addOption("accesskey", true, "Primary Access Key");
		addOption("containername", true, "Storage Container Name");
		addOption("containeraccess", true, "Access level of Storage Container");
		
	}

	/* (non-Javadoc)
	 * @see com.automic.azure.actions.AbstractAction#initialize()
	 */
	@Override
	protected void initialize() {
		// storage acc from account name and access key
		this.storageAccount =  new AzureStorageAccount(getOptionValue("accountname"), getOptionValue("accesskey"));
		// container Name
		this.containerName = getOptionValue("containername");
		// access level of container
		this.containerAccess = ContainerAccess.valueOf(getOptionValue("containeraccess"));
		// authentication service
		this.authenticationService = new AzureStorageAuthenticationService(storageAccount, false);
		
		
		
	}

	/* (non-Javadoc)
	 * @see com.automic.azure.actions.AbstractAction#validateInputs()
	 */
	@Override
	protected void validateInputs() throws AzureException {
		
		// validate storage name 
		if (!Validator.checkNotEmpty(storageAccount.getAccountName())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
        }
		
		// validate storage access key
		if (!Validator.checkNotEmpty(storageAccount.getPrimaryAccessKey())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
        }
		
		// validate storage container name
        if (!Validator.checkNotEmpty(this.containerName)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
        }
        
        // validate storage container access
        if (!Validator.checkNotNull(this.containerAccess)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_ACCESS);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_ACCESS);
        }
	}

	/* (non-Javadoc)
	 * @see com.automic.azure.actions.AbstractAction#executeSpecific(com.sun.jersey.api.client.Client)
	 */
	@Override
	protected ClientResponse executeSpecific(Client storageHttpClient)
			throws AzureException {
		
		//
		WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName);
		
		this.authenticationService.addCommonHttpHeaders("VERB", "PUT");
		this.authenticationService.addCommonHttpHeaders("Content-Type", "text/plain");
		// header for container access
		this.authenticationService.addStorageHttpHeaders("x-ms-blob-public-access", containerAccess.getValue());
		// add storage HTTP headers
		this.authenticationService.addStorageHttpHeaders("x-ms-version", getOptionValue("xmsversion"));
		this.authenticationService.addStorageHttpHeaders("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
		//add query parameters
		this.authenticationService.addQueryParameter("restype", "container");
		// update URI
		//clientURIForSignature = clientURIForSignature + "/" + containerName;
		// calculate Authorization header
		//storageHttpHeaders.put("Authorization", this.authenticationService.createAuthorizationHeader());
		
		// set query parameters
		Map<String, String> queryParameters = this.authenticationService.getQueryParameters();
		for(String headerKey : queryParameters.keySet()){
			resource = resource.queryParam(headerKey, queryParameters.get(headerKey));
		}
		
		WebResource.Builder builder = resource.getRequestBuilder();
		// set storage headers
		Map<String, String> storageHttpHeaders = this.authenticationService.getStorageHttpHeaders();
		for(String headerKey : storageHttpHeaders.keySet()){
			builder = builder.header(headerKey, storageHttpHeaders.get(headerKey));
		}
		
		
		
		// call the create container service and return response 
		return builder.put(ClientResponse.class, Strings.EMPTY);
		
	}

	/* (non-Javadoc)
	 * @see com.automic.azure.actions.AbstractAction#prepareOutput(com.sun.jersey.api.client.ClientResponse)
	 */
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		// TODO Auto-generated method stub
		System.out.println("Response:" + response.getClientResponseStatus().getReasonPhrase());
		//System.out.println("XML:"+ response.getEntity(String.class));

		// write formatted xml to System console
		try {
			CommonUtil.copyData(new ByteArrayInputStream(response.getEntity(String.class).getBytes()),
					new FileOutputStream("D://testResponse.xml"));
		} catch (AzureException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
