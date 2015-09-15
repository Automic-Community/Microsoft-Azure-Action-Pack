/**
 * 
 */
package com.automic.azure.actions;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
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

    /**
     * The Delete Container action marks the specified container for deletion. The container and any blobs contained
     * within it are later deleted during garbage collection. To call this action on a container that has an active
     * lease, specify the lease ID.If no value is provided for lease id when there is an active lease, Delete Container
     * action will return 409 (Conflict).If wrong lease ID is provided or a lease ID is provided for a container that
     * does not have an active lease Delete Container action will return 412 (Precondition failed).
     * */
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

        if (Validator.checkNotEmpty(this.leaseId)) {
            builder = builder.header("x-ms-lease-id", leaseId);
        }
        prepareOutput(builder.delete(ClientResponse.class));
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
        } else if (!this.containerName.matches("[0-9a-z]+")) {
            LOGGER.error(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
        }

    }

    private void prepareOutput(ClientResponse response) throws AzureException {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));
    }
}
