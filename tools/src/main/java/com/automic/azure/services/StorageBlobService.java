/**
 * 
 */
package com.automic.azure.services;

import java.util.Map;

import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.ContainerAccess;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureStorageAccount;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Class as a service to interact with Azure Storage Blob Service
 *
 */
public class StorageBlobService extends AzureStorageService {

	/**
	 * @param storageAccount
	 * @param commonHeaders
	 * @param isServiceForTable
	 */
	public StorageBlobService(AzureStorageAccount storageAccount,
			Map<String, String> commonHeaders, boolean isServiceForTable) {
		super(storageAccount, commonHeaders, isServiceForTable);
		
	}
	
	
	
	/**
	 * Method to Create a Container in Storage Service
	 * 
	 * @param containerName
	 * @param containerAccess
	 * @param storageHttpClient
	 * @return
	 * @throws AzureException 
	 */
	public ClientResponse createContainer(String containerName, ContainerAccess containerAccess, Client storageHttpClient) throws AzureException {
		//
		WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName);
		
		// header for container access
		storageHttpHeaders.put("x-ms-blob-public-access", containerAccess.getValue());
		// update URI
		clientURIForSignature = clientURIForSignature + "/" + containerName;
		// calculate Authorization header
		storageHttpHeaders.put("Authorization", createAuthorizationHeader());
		
		// set query parameters
		for(String headerKey : queryParameters.keySet()){
			resource = resource.queryParam(headerKey, queryParameters.get(headerKey));
		}
		
		WebResource.Builder builder = resource.getRequestBuilder();
		// set storage headers
		for(String headerKey : storageHttpHeaders.keySet()){
			builder = builder.header(headerKey, storageHttpHeaders.get(headerKey));
		}
		
		
		
		// call the create container service and return response 
		return builder.put(ClientResponse.class, Strings.EMPTY);
	}

}
