/**
 * 
 */
package com.automic.azure.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ContainerAccess;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.services.AzureStorageService;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Action class to create a Container in Azure Storage
 *
 */
public class CreateStorageContainerAction extends AbstractStorageAction {

	
	private static final Logger LOGGER = LogManager.getLogger(CreateStorageContainerAction.class);
	
	/**
	 * Storage Service
	 */
	private AzureStorageService storageService;
	
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
		AzureStorageAccount storageAccount = new AzureStorageAccount(getOptionValue("accountname"), getOptionValue("accesskey"));
		Map<String , String> commonHeaders = new HashMap<>();
		commonHeaders.put("VERB", "PUT");
		commonHeaders.put("Content-Type", "text/plain");
		this.storageService = new AzureStorageService(storageAccount, commonHeaders, false);
		// add storage HTTP headers
		this.storageService.addStorageHttpHeaders("x-ms-version", getOptionValue("x-ms-version"));
		this.storageService.addStorageHttpHeaders("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
		
		//add query parameters
		this.storageService.addQueryParameter("restype", "container");
		// container Name
		this.containerName = getOptionValue("containername");
		// access level of container
		this.containerAccess = ContainerAccess.valueOf(getOptionValue("containeraccess"));
		
	}

	/* (non-Javadoc)
	 * @see com.automic.azure.actions.AbstractAction#validateInputs()
	 */
	@Override
	protected void validateInputs() throws AzureException {
		// validate storage name and container name
		if (!Validator.checkNotEmpty(getOptionValue("accountname"))) {
            LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
            throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
        }
        if (!Validator.checkNotEmpty(this.containerName)) {
            LOGGER.error(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
            throw new AzureException(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
        }
        
        if (!Validator.checkNotNull(this.containerAccess)) {
            LOGGER.error(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
            throw new AzureException(ExceptionConstants.EMPTY_REQUEST_TOKEN_ID);
        }
	}

	/* (non-Javadoc)
	 * @see com.automic.azure.actions.AbstractAction#executeSpecific(com.sun.jersey.api.client.Client)
	 */
	@Override
	protected ClientResponse executeSpecific(Client client)
			throws AzureException {
		
		return storageService.createContainer(this.containerName, this.containerAccess, client);
	}

	/* (non-Javadoc)
	 * @see com.automic.azure.actions.AbstractAction#prepareOutput(com.sun.jersey.api.client.ClientResponse)
	 */
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		// TODO Auto-generated method stub

	}

}
