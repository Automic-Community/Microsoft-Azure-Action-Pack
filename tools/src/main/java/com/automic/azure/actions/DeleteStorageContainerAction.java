/**
 * 
 */
package com.automic.azure.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * @author sumitsamson
 * 
 */
public class DeleteStorageContainerAction extends AbstractStorageAction {

	private static final Logger LOGGER = LogManager.getLogger(DeleteStorageContainerAction.class);

	/**
	 * Storage container name
	 */
	private String containerName;
	/**
	 * Storage lease Id
	 */
	private String leaseId;

	public DeleteStorageContainerAction() {
		addOption("containername", true, "Storage Container Name");
		addOption("leaseid", false, "Storage Lease Id");
	}

	@Override
	protected void executeSpecific(Client storageHttpClient) throws AzureException {
		initialize();
		validate();

		WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
				.queryParam("restype", "container");

		WebResource.Builder builder = resource.entity(Strings.EMPTY, "text/plain")
				.header("x-ms-version", this.restapiVersion)
				.header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());

		LOGGER.info("Calling URL:" + resource.getURI());
		if (!this.leaseId.isEmpty()) {
			builder = builder.header("x-ms-lease-id", leaseId);
		}

	}

	private void initialize() {
		this.containerName = getOptionValue("containername");
		this.leaseId = getOptionValue("leaseid");

	}

	private void validate() throws AzureException {
		// validate storage container name
		if (!Validator.checkNotEmpty(this.containerName)) {
			LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
		}

	}

}
