package com.automic.azure.actions;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * Class to delete an existing blob from a storage container. The Delete Blob operation marks the specified blob or
 * snapshot for deletion. The blob is later deleted during garbage collection.
 * 
 * @author shrutinambiar
 *
 */
public class DeleteBlobAction extends AbstractStorageAction {

    private static final Logger LOGGER = LogManager.getLogger(DeleteBlobAction.class);

    private String containerName;
    private String blobName;
    private String snapshot;
    private String leaseId;

    public DeleteBlobAction() {
        addOption("containername", true, "Name of the storage container");
        addOption("blobname", true, "Name of the blob to be deleted");
        addOption("snapshot", false, "Specific date-time snapshot of the Blob to delete");
        addOption("leaseid", false, "Lease ID if blob has an active lease");
    }

    /**
     * Method to make a call to Azure Management API. To delete a blob we make a delete request to
     * https://myaccount.blob.core.windows.net/mycontainer/myblob
     * https://myaccount.blob.core.windows.net/mycontainer/myblob?snapshot=<DateTime>
     */
    @Override
    protected void executeSpecific(Client storageHttpClient) throws AzureException {
        initialize();
        validate();
        WebResource webResource = storageHttpClient.resource(storageAccount.blobURL()).path(containerName)
                .path(blobName);

        WebResource.Builder webBuilder = null;

        /*
         * If snapshot value is 'include', it will delete the blob along with its snapshot(s) else it will delete only
         * the snapshot(s).
         */
        if (("include").equals(snapshot) || ("only").equals(snapshot)) {
            webBuilder = webResource.header("x-ms-delete-snapshots", snapshot);
        } else {
            /* delete one particular snapshot of a blob */
            webBuilder = webResource.queryParam("snapshot", snapshot).getRequestBuilder();
        }

        webBuilder = webBuilder.header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService())
                .header("x-ms-version", restapiVersion).header("x-ms-lease-id", leaseId);

        LOGGER.info("Calling URL:" + webResource.getURI());
        ClientResponse response = webBuilder.delete(ClientResponse.class);
        prepareOutput(response);
    }

    private void initialize() {
        containerName = getOptionValue("containername");
        blobName = getOptionValue("blobname");

        String snapshotValue = getOptionValue("snapshot");
        snapshot = "include";
        if (Validator.checkNotEmpty(snapshotValue)) {
            if (("*").equals(snapshotValue)) {
                snapshot = "only";
            } else {
                snapshot = snapshotValue;
            }
        }
    }

    private void validate() throws AzureException {
        if (!Validator.checkNotEmpty(containerName)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
        } else if (!containerName.matches(Constants.CONTAINER_NAME_REGEX)) {
            LOGGER.error(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
        }

        if (!Validator.checkNotEmpty(blobName)) {
            LOGGER.error(ExceptionConstants.EMPTY_BLOB_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_BLOB_NAME);
        } else if (!blobName.matches(Constants.BLOB_NAME_REGEX) || blobName.length() > 1024) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_NAME);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_NAME);
        }
    }

    private void prepareOutput(ClientResponse response) {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));
    }

}
