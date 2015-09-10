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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
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

    /**
     * 
     * Utility class to generate blockid
     *
     */
    private static class BlockIdGenerator {
        private static int blockId = 0;
        private static ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE);

        public static synchronized String generateBlockIdBase64encoded() throws UnsupportedEncodingException {
            buffer.putInt(0, ++blockId);
            byte[] blockIdBase64 = Base64.encode(buffer.array());
            return new String(blockIdBase64, "UTF-8");
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(PutBlockBlobAction.class);
    // size of block blob 4 MB
    private static final int BLOCK_SIZE = 4194304;

    // max size of blob 195 GB
    private static final long MAX_BLOB_SIZE = 209379655680L;

    // min size of blob to be uploaded as a block blob 64 MB
    private static final long FILE_SIZE_FOR_BLOCK_UPLOAD = 67108864L;

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
     * Blob Id List file
     */
    private Path blobIdListFile;

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
        // validate the inputs
        validate();

        // initialize the inputs
        initialize();

        // if file size is greated than 64MB we upload using block blob
        if (this.fileSize > FILE_SIZE_FOR_BLOCK_UPLOAD) {
            LOGGER.info(String.format("File size %s MB larger than 64 MB: ", fileSize / 1048576));

            try {
                long startTime = System.currentTimeMillis();
                // blockid list file
                this.blobIdListFile = generateBlockIdListFile();
                // upload as block of 4MB
                uploadBlockBlobInBlocks(storageHttpClient);
                // commit blob with all blockids
                commitBlockList(storageHttpClient);
                long endTime = System.currentTimeMillis();
                LOGGER.info("Block Blob uploaded in millisec: " + (endTime - startTime));
            } finally {
                deleteBlockIdListXML();
            }

        } else {
            LOGGER.info(String.format("File size %s KB less than 64 MB: ", fileSize / 1024));
            uploadBlockBlob(storageHttpClient);

        }

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
            blockIdListXml = new BufferedOutputStream(Files.newOutputStream(blobIdListFile), 600);
            blockIdListXml.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>".getBytes("UTF-8"));
            blockIdListXml.write("\n".getBytes("UTF-8"));
            blockIdListXml.write(BLOCKID_XML_ROOT_ELEMENT.getBytes("UTF-8"));
            blockIdListXml.write("\n".getBytes("UTF-8"));
            LOGGER.info("uploading blob in chunks of 4MB blocks!");
            while ((blockSize = inputStream.read(fileBlock)) != -1) {
                // generate blockid
                blockId = BlockIdGenerator.generateBlockIdBase64encoded();
                // add blockid to xml file to commit later
                blockIdListXml.write(BLOCKID_XML_ELEMENT.replace("BLOCK_ID", blockId).getBytes("UTF-8"));
                blockIdListXml.write("\n".getBytes("UTF-8"));
                // set query parameters and headers and upload block of 4MB
                resource.queryParam("blockid", blockId).header("Content-Length", blockSize)
                        .header("x-ms-version", PutBlockBlobAction.this.restapiVersion)
                        .header("x-ms-blob-type", "BlockBlob")
                        .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService())
                        .entity(Arrays.copyOfRange(fileBlock, 0, blockSize), contentType).put(ClientResponse.class);

            }
            blockIdListXml.write(BLOCKID_XML_ROOT_END_ELEMENT.getBytes("UTF-8"));

        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD, e);
            throw new AzureException(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD);
        } finally {
            // close stream for block Id xml
            try {
                blockIdListXml.close();
            } catch (IOException e) {
                LOGGER.error(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD, e);
                throw new AzureException(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD);
            }
        }

    }

    //
    private void commitBlockList(Client storageHttpClient) throws AzureException {
        // get URL
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
                .path(blobName).queryParam("comp", "blocklist");
        LOGGER.info("Commiting the blocks to the new Blob with name " + this.blobName);
        // set query parameters and headers
        WebResource.Builder builder = null;
        try {
            builder = resource.header("Content-Length", Files.size(blobIdListFile))
                    .header("x-ms-version", this.restapiVersion)
                    .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.ERROR_COMMITING_BLOCK_BLOB, e);
            throw new AzureException(ExceptionConstants.ERROR_COMMITING_BLOCK_BLOB);
        }

        LOGGER.info("Calling URL:" + resource.getURI());
        // call the create container service and return response
        builder.entity(new File(blobIdListFile.toString()), MediaType.APPLICATION_XML).put(ClientResponse.class);

    }

    // upload blob as a single entity
    private void uploadBlockBlob(Client storageHttpClient) {
        // get URL
        long startTime = System.currentTimeMillis();
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
                .path(blobName);
        // set query parameters and headers
        WebResource.Builder builder = resource
                // .queryParam("restype", "container")
                .header("Content-Length", this.fileSize).header("x-ms-version", this.restapiVersion)
                .header("x-ms-blob-type", "BlockBlob")
                .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());

        LOGGER.info("Calling URL:" + resource.getURI());
        // call the create container service and return response
        builder.entity(blobFile.toFile(), contentType).put(ClientResponse.class);
        long endTime = System.currentTimeMillis();
        LOGGER.info("Blob uploaded as single thread in millisec:" + (endTime - startTime));
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

        // reads the size of file from its attributes
        try {
            this.fileSize = Files.size(blobFile);
            // if size of file is greater than 195 GB
            if (fileSize > MAX_BLOB_SIZE) {
                String msg = String.format(ExceptionConstants.ERROR_BLOB_MAX_SIZE, MAX_BLOB_SIZE / 1073741824);
                LOGGER.error(msg);
                throw new AzureException(msg);
            }
        } catch (IOException e) {
            String msg = String.format(ExceptionConstants.ERROR_FILE_SIZE, blobFile.toString());
            LOGGER.error(msg, e);
            throw new AzureException(msg);
        }

        // blob content-type
        String contentTypeArgs = getOptionValue(Constants.CONTENT_TYPE);
        this.contentType = Validator.checkNotEmpty(contentTypeArgs) ? contentTypeArgs
                : MediaType.APPLICATION_OCTET_STREAM;

    }

    private void validate() throws AzureException {
        // validate storage container name
        String containerNameArgs = getOptionValue(Constants.CONTAINER_NAME);
        if (!Validator.checkNotEmpty(containerNameArgs)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
        } else if (!Validator.isStorageContainerNameValid(containerNameArgs)) {
            LOGGER.error(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.INVALID_STORAGE_CONTAINER_NAME);
        }

        // validate blob file
        if (!Validator.checkFileExists(getOptionValue("blobfile"))) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_FILE);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_FILE);
        }

        // validate content-type
        String contentTypeArgs = getOptionValue(Constants.CONTENT_TYPE);
        if (Validator.checkNotEmpty(contentTypeArgs)) {
            try {
                MediaType.valueOf(contentTypeArgs);
            } catch (IllegalArgumentException e) {

                LOGGER.error(ExceptionConstants.INVALID_BLOB_CONTENT_TYPE);
                throw new AzureException(ExceptionConstants.INVALID_BLOB_CONTENT_TYPE);
            }
        }
        // validate blob name
        String blobNameArgs = getOptionValue("blobname");
        if (Validator.checkNotEmpty(blobNameArgs) && !Validator.isContainerBlobNameValid(blobNameArgs)) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_NAME);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_NAME);
        }
    }

    // generate name for Block id List file
    private Path generateBlockIdListFile() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String dateStr = dateFormat.format(cal.getTime());
        return Paths.get(System.getProperty("user.dir") + File.separator + "blockIdList" + dateStr + ".xml");
    }

    // delete the block id list file generated
    private void deleteBlockIdListXML() {
        // TODO Auto-generated method stub

    }
}
