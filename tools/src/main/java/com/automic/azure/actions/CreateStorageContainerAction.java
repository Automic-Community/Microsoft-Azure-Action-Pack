/**
 * 
 */
package com.automic.azure.actions;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ContainerAccess;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.httpfilters.StorageAuthenticationFilter;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;
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

    /**
	 * 
	 */
    public CreateStorageContainerAction() {
        addOption("containername", true, "Storage Container Name");
        addOption("containeraccess", true, "Access level of Storage Container. "
                + "possible values PRIVATE, CONTAINER or BLOB");
    }

    @Override
    protected void initializeActionSpecificArgs() {
        // storage acc from account name and access key
        this.storageAccount = new AzureStorageAccount(getOptionValue("storage"), getOptionValue("accesskey"));
        // container Name
        this.containerName = getOptionValue("containername");
        // access level of container
        if (getOptionValue("containeraccess") != null) {
            this.containerAccess = ContainerAccess.valueOf(getOptionValue("containeraccess"));
        }

    }

    @Override
    protected void validateActionSpecificInputs() throws AzureException {

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

    /**
     * {@inheritDoc com.automic.azure.actions.AbstractAction#executeSpecific }. Method makes PUT request to
     * https://myaccount.blob.core.windows.net/mycontainer?restype=container
     * 
     */
    @Override
    protected ClientResponse executeSpecific(Client storageHttpClient) throws AzureException {
        // add authorisation filter to client
        storageHttpClient.addFilter(new StorageAuthenticationFilter(storageAccount, false));
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
        return builder.entity(Strings.EMPTY, "text/plain").put(ClientResponse.class);

    }

    /**
     * 
     * {@inheritDoc com.automic.azure.actions.AbstractAction#prepareOutput} Method publishes the request id returned by
     * the REST api
     * 
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws AzureException {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID ::=" + tokenid.get(0));
    }


}
