/**
 * 
 */
package com.automic.azure.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.exception.util.AzureRejectedExecutionHandler;
import com.automic.azure.model.AzurePutBlockBlobList;
import com.automic.azure.util.AzureThreadFactory;
import com.automic.azure.util.AzureThreadPoolExecutor;
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
    private static final Logger THREAD_LOGGER = LogManager.getLogger(BlockUploadTask.class);

    private static final int THREAD_POOL_SIZE = 4;

    private static final int WORKER_QUEUE_SIZE = 6;

    private static final int THREADPOOL_TERMINATE_TIMEOUT = 60;

    // 4 MB
    private static final int BLOCK_SIZE = 4194304;

    // 195 GB
    private static final long MAX_BLOB_SIZE = 209379655680L;

    // 64 MB
    private static final long FILE_SIZE_FOR_BLOCK_UPLOAD = 67108864L;

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
     * 
     */
    private List<String> blockIdList = new ArrayList<>();

    /**
     * 
     * Inner Class which defines a Runnable to send an Http request in a separate thread
     *
     */
    class BlockUploadTask implements Runnable {

        private WebResource resource;
        private String blockId;
        private byte[] fileBlock;
        private int blockSize;

        public BlockUploadTask(WebResource blockUploadResource, int blockSize, byte[] fileBlock, String blockId) {
            this.resource = blockUploadResource;
            this.blockId = blockId;
            this.fileBlock = fileBlock;
            this.blockSize = blockSize;
        }

        @Override
        public void run() {
            // set query parameters and headers
            WebResource blockUploadResource = resource.queryParam("blockid", blockId);
            WebResource.Builder builder = blockUploadResource.header("Content-Length", blockSize)
                    .header("x-ms-version", PutBlockBlobAction.this.restapiVersion)
                    .header("x-ms-blob-type", "BlockBlob")
                    .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService())
                    .entity(Arrays.copyOfRange(fileBlock, 0, blockSize), contentType);
            THREAD_LOGGER.info("Calling URL: " + blockUploadResource.getURI());
            Thread.currentThread();
            builder.put(ClientResponse.class);
        }
    }

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
            uploadBlockBlobInBlocks(storageHttpClient);
            commitBlockList(storageHttpClient);

        } else {
            LOGGER.info(String.format("File size %s MB less than 64 MB: ", fileSize / 1048576));
            uploadBlockBlob(storageHttpClient);

        }

    }

    //
    private void uploadBlockBlobInBlocks(Client storageHttpClient) throws AzureException {
        // create URL to upload block blob
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
                .path(blobName).queryParam("comp", "block");
        byte[] fileBlock = new byte[BLOCK_SIZE];
        try (InputStream inputStream = Files.newInputStream(blobFile)) {
            int blockSize;
            String blockId = null;
            // create a thread pool of 4 threads
            AzureThreadPoolExecutor executor = new AzureThreadPoolExecutor(THREAD_POOL_SIZE, WORKER_QUEUE_SIZE,
                    THREADPOOL_TERMINATE_TIMEOUT, new AzureThreadFactory(), TimeUnit.MINUTES);
            // set an rejected execution handler
            executor.setRejectedExecutionHandler(new AzureRejectedExecutionHandler());
            LOGGER.info("uploading blob in chunks of 4MB blocks!");
            while ((blockSize = inputStream.read(fileBlock)) != -1) {
                // generate blockid
                blockId = BlockIdGenerator.generateBlockIdBase64encoded();
                // add blockid to a list to commit later
                this.blockIdList.add(blockId);
                // upload block in a separate thread
                Runnable newBlockUploadThread = new BlockUploadTask(resource, blockSize, fileBlock, blockId);
                executor.execute(newBlockUploadThread);

            }
            // wait for all blocks to be uploaded
            executor.shutDownAndTerminate();

        } catch (InterruptedException | IOException e) {
            LOGGER.error(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD, e);
            throw new AzureException(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD);
        }

    }

    //
    private void commitBlockList(Client storageHttpClient) throws AzureException {
        // get URL
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName)
                .path(blobName).queryParam("comp", "blocklist");
        AzurePutBlockBlobList blockList = new AzurePutBlockBlobList();
        LOGGER.info("Commiting the blocks");
        blockList.setCode(blockIdList);
        // set query parameters and headers
        WebResource.Builder builder = null;
        try {
            builder = resource.header("Content-Length", blockList.getJaxbLength())
                    .header("x-ms-version", this.restapiVersion)
                    .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
        } catch (JAXBException e) {
            LOGGER.error(ExceptionConstants.ERROR_COMMITING_BLOCK_BLOB, e);
            throw new AzureException(ExceptionConstants.ERROR_COMMITING_BLOCK_BLOB);
        }

        LOGGER.info("Calling URL:" + resource.getURI());
        LOGGER.info("Block id to commit:" + blockList);
        // call the create container service and return response
        builder.entity(blockList, MediaType.APPLICATION_XML).put(ClientResponse.class);

    }

    private void uploadBlockBlob(Client storageHttpClient) {
        // get URL
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

    }

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
        this.contentType = Validator.checkNotEmpty(getOptionValue(Constants.CONTENT_TYPE)) 
                ? getOptionValue(Constants.CONTENT_TYPE)
                : MediaType.APPLICATION_OCTET_STREAM;

    }

    private void validate() throws AzureException {
        // validate storage container name
        if (!Validator.checkNotEmpty(getOptionValue(Constants.CONTAINER_NAME))) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
        } else if (!getOptionValue(Constants.CONTAINER_NAME).matches("[0-9a-z]{3,63}")) {
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
        String blobNameArgs = getOptionValue("blobname");
        if (Validator.checkNotEmpty(blobNameArgs)
                && (!blobNameArgs.matches("[a-zA-Z0-9_.\\-\\+\\$\\&\\,\\/\\:\\;\\=\\?\\@]+\\.[a-zA-Z]+") || blobNameArgs
                        .length() > 1024)) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_NAME);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_NAME);
        }
    }
}
