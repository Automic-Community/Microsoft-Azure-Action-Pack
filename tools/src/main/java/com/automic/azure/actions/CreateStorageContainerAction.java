/**
 * 
 */
package com.automic.azure.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.ContainerAccess;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureBusinessException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to create a Container in Azure Storage
 *
 */
public final class CreateStorageContainerAction extends AbstractStorageAction {

    private static final Logger LOGGER = LogManager.getLogger(CreateStorageContainerAction.class);

    /**
     * Storage container name
     */
    private String containerName;

    /**
     * Container access
     */
    private ContainerAccess containerAccess;

    public CreateStorageContainerAction() {
        addOption("containername", true, "Storage Container Name");
        addOption("containeraccess", true, "Access level of Storage Container. "
                + "possible values PRIVATE, CONTAINER or BLOB");
    }

    /**
     * Method makes PUT request to https://myaccount.blob.core.windows.net/mycontainer?restype=container
     * 
     */
    @Override
    public void executeSpecific(Client storageHttpClient) throws AzureBusinessException {
        initialize();
        validate();
        // get URL
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName);
        // set query parameters and headers
        WebResource.Builder builder = resource.queryParam("restype", "container")
                .header("x-ms-version", this.restapiVersion)
                .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
        // header for container access
        if (containerAccess != null && !ContainerAccess.PRIVATE.equals(containerAccess)) {
            builder = builder.header("x-ms-blob-public-access", containerAccess.getValue());
        }
        LOGGER.info("Calling URL:" + resource.getURI());
        // call the create container service and return response
        builder.entity(Strings.EMPTY, "text/plain").put(ClientResponse.class);
    }

    private void initialize() {
        // container Name
        this.containerName = getOptionValue("containername");
        // access level of container
        if (getOptionValue("containeraccess") != null) {
            this.containerAccess = ContainerAccess.valueOf(getOptionValue("containeraccess"));
        }
    }

    private void validate() throws AzureBusinessException {
        // validate storage container name
        if (!Validator.checkNotEmpty(this.containerName)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
            throw new AzureBusinessException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
        } else if (!this.containerName.matches("[0-9a-z]+")) {
            LOGGER.error(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
            throw new AzureBusinessException(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
        }

        // validate storage container access
        if (!Validator.checkNotNull(this.containerAccess)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_ACCESS);
            throw new AzureBusinessException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_ACCESS);
        }
    }

}
