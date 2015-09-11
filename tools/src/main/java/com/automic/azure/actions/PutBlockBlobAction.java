/**
 * 
 */
package com.automic.azure.actions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

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
import com.sun.jersey.core.util.Base64;

/**
 * Action class to create a Container in Azure Storage
 *
 */
public final class PutBlockBlobAction extends AbstractStorageAction {

    private static final String UT8_ENCODING = "UTF-8";

    /**
     * 
     * Utility class to generate blockid
     *
     */
    private static class BlockIdGenerator {
        private static short blockId = 0;
        private static ByteBuffer buffer = ByteBuffer.allocate(Short.SIZE);

        public static String generateBlockIdBase64encoded() throws UnsupportedEncodingException {
            buffer.putShort(0, ++blockId);
            byte[] blockIdBase64 = Base64.encode(buffer.array());
            return new String(blockIdBase64, UT8_ENCODING);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(PutBlockBlobAction.class);
    // size of block blob 4 MB
    private static final int BLOCK_SIZE = 4 * 1024 * 1024;

    // max size of blob 195 GB
    private static final long MAX_BLOB_SIZE = 195L * 1024 * 1024 * 1024;

    // min size of blob to be uploaded as a block blob 64 MB
    private static final long FILE_SIZE_FOR_BLOCK_UPLOAD = 64L * 1024 * 1024;

    private static final String BLOCKID_XML_ROOT_ELEMENT = "<BlockList>";

    private static final String BLOCKID_XML_ROOT_END_ELEMENT = "</BlockList>";

    private static final String BLOCKID_XML_ELEMENT = "<Latest>BLOCK_ID</Latest>";

    /**
     * Storage container name
     */
    private String containerName;

    /**
     * Container Blob
     */
    private String blobName;

    /**
     * Blob file
     */
    private Path blobFile;

    /**
     * 
     */
    private String contentType;

    /**
     * size of blob file
     */
    private long fileSize;

    /**
     * Block Id List file
     */
    private Path blockIdListFile;

    public PutBlockBlobAction() {
        addOption(Constants.CONTAINER_NAME, true, "Storage Container Name");
        addOption("blobname", false, "Container Blob Name");
        addOption("blobfile", true, "Blob file path");
        addOption(Constants.CONTENT_TYPE, false, "Content-Type of the blob file");
    }

    /**
     * Method makes PUT request to https://myaccount.blob.core.windows.net/mycontainer/myblob
     * 
     */
    @Override
    public void executeSpecific(Client storageHttpClient) throws AzureException {
        // initialize the inputs
        initialize();

        // validate the inputs
        validate();

        ClientResponse response = null;
        // if file size is greated than 64MB we upload using block blob
        if (this.fileSize > FILE_SIZE_FOR_BLOCK_UPLOAD) {
            LOGGER.info(String.format("File size %s bytes larger than 64 MB: ", fileSize));

            try {
                // blockid list file
                this.blockIdListFile = generateBlockIdListFile();
                LOGGER.info("Initializing block id list xml file: " + this.blockIdListFile);
                // upload as block of 4MB
                uploadBlockBlobInBlocks(storageHttpClient);
                // commit blob with all blockids
                response = commitBlockList(storageHttpClient);
                
            } finally {
                deleteBlockIdListXML();
            }

        } else {
            LOGGER.info(String.format("File size %s bytes less than 64 MB: ", fileSize));
            response = uploadBlockBlob(storageHttpClient);

        }
        LOGGER.info("Blob %s uploaded succesfully.", this.blobName);
        // publish blob name and print request id in AE report
        prepareOutput(response);

    }

    //
    private void uploadBlockBlobInBlocks(Client storageHttpClient) throws AzureException {
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
                .path(blobName).queryParam("comp", "block");
        // define fileblock
        byte[] fileBlock = new byte[BLOCK_SIZE];
        BufferedOutputStream blockIdListXml = null;
        // create client config
        try (InputStream inputStream = Files.newInputStream(blobFile)) {
            int blockSize;
            String blockId = null;
            blockIdListXml = new BufferedOutputStream(Files.newOutputStream(blockIdListFile), 600);
            blockIdListXml.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>".getBytes(UT8_ENCODING));
            blockIdListXml.write(BLOCKID_XML_ROOT_ELEMENT.getBytes(UT8_ENCODING));
            LOGGER.info("uploading blob in chunks of 4MB blocks!");
            while ((blockSize = inputStream.read(fileBlock)) != -1) {
                // generate blockid
                blockId = BlockIdGenerator.generateBlockIdBase64encoded();
                // add blockid to xml file to commit later
                blockIdListXml.write(BLOCKID_XML_ELEMENT.replace("BLOCK_ID", blockId).getBytes(UT8_ENCODING));
                // set query parameters and headers and upload block of 4MB
                resource.queryParam("blockid", blockId).header("Content-Length", blockSize)
                        .header("x-ms-version", PutBlockBlobAction.this.restapiVersion)
                        .header("x-ms-blob-type", "BlockBlob")
                        .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService())
                        .entity(Arrays.copyOfRange(fileBlock, 0, blockSize), contentType).put(ClientResponse.class);

            }
            blockIdListXml.write(BLOCKID_XML_ROOT_END_ELEMENT.getBytes(UT8_ENCODING));

        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD, e);
            throw new AzureException(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD);
        } finally {
            // close stream for block Id xml
            try {
                if (blockIdListXml != null) {
                    blockIdListXml.close();
                }
            } catch (IOException e) {
                LOGGER.error(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD, e);
                throw new AzureException(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD);
            }
        }

    }

    //
    private ClientResponse commitBlockList(Client storageHttpClient) throws AzureException {
        // get URL
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
                .path(blobName).queryParam("comp", "blocklist");
        LOGGER.info("Commiting the blocks to the new Blob with name " + this.blobName);
        // set query parameters and headers
        WebResource.Builder builder = null;
        try {
            builder = resource.header("Content-Length", Files.size(blockIdListFile))
                    .header("x-ms-version", this.restapiVersion)
                    .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.ERROR_COMMITING_BLOCK_BLOB, e);
            throw new AzureException(ExceptionConstants.ERROR_COMMITING_BLOCK_BLOB);
        }

        LOGGER.info("Calling URL:" + resource.getURI());
        // call the create container service and return response
        return builder.entity(new File(blockIdListFile.toString()), MediaType.APPLICATION_XML)
                .put(ClientResponse.class);

    }

    // upload blob as a single entity
    private ClientResponse uploadBlockBlob(Client storageHttpClient) {
        // get URL
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
                .path(blobName);
        // set query parameters and headers
        WebResource.Builder builder = resource.header("Content-Length", this.fileSize)
                .header("x-ms-version", this.restapiVersion).header("x-ms-blob-type", "BlockBlob")
                .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());

        LOGGER.info("Calling URL:" + resource.getURI());
        // call the create container service and return response
        return builder.entity(blobFile.toFile(), contentType).put(ClientResponse.class);
    }

    // initialize the parameters
    private void initialize() throws AzureException {
        // container Name
        this.containerName = getOptionValue(Constants.CONTAINER_NAME);
        // blob file
        this.blobFile = Paths.get(getOptionValue("blobfile"));
        // Blob Name
        this.blobName = getOptionValue("blobname");
        // construct the blob name from blob file path
        if (!Validator.checkNotEmpty(this.blobName) && this.blobFile != null) {
            this.blobName = blobFile.getFileName().toString();
        }

        // blob content-type
        String contentTypeArgs = getOptionValue(Constants.CONTENT_TYPE);
        this.contentType = Validator.checkNotEmpty(contentTypeArgs) ? contentTypeArgs
                : MediaType.APPLICATION_OCTET_STREAM;

    }

    // validate the parameters
    private void validate() throws AzureException {
        // validate storage container name
        if (!Validator.isStorageContainerNameValid(this.containerName)) {
            LOGGER.error(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
        }

        // validate blob file
        if (!Validator.checkFileExists(getOptionValue("blobfile"))) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_FILE);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_FILE);
        }

        // validate content-type
        try {
            MediaType.valueOf(this.contentType);
        } catch (IllegalArgumentException e) {

            LOGGER.error(ExceptionConstants.INVALID_BLOB_CONTENT_TYPE);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_CONTENT_TYPE);
        }

        // validate blob name
        if (!Validator.isContainerBlobNameValid(this.blobName)) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_NAME);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_NAME);
        }

        // reads the size of file from its attributes
        try {
            this.fileSize = Files.size(this.blobFile);
            // if size of file is greater than 195 GB
            if (fileSize > MAX_BLOB_SIZE) {
                String msg = String.format(ExceptionConstants.ERROR_BLOB_MAX_SIZE, MAX_BLOB_SIZE / 1073741824);
                LOGGER.error(msg);
                throw new AzureException(msg);
            }
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_FILE);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_FILE);
        }
    }

    // generate Block id List file based on blob name
    private Path generateBlockIdListFile() {
        return Paths.get(System.getProperty("user.dir") + File.separator + "blockIdList" + this.blobName + ".xml");
    }

    // delete the block id list file generated
    private void deleteBlockIdListXML() throws AzureException {

        LOGGER.info("Deleting block id list xml file: " + this.blockIdListFile);
        try {
            Files.delete(this.blockIdListFile);
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.ERROR_DELETING_BLOCKID_FILE, e);
            throw new AzureException(ExceptionConstants.ERROR_DELETING_BLOCKID_FILE);
        }
    }

    // publish blob name and print request id in AE report
    private void prepareOutput(ClientResponse response) {
        // publish blob name
        ConsoleWriter.writeln("UC4RB_AZR_BLOB_NAME ::=" + this.blobName);
        // request id
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("Request ID : " + tokenid.get(0));
    }
}
