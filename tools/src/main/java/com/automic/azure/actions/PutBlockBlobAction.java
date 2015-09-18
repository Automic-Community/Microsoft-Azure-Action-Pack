/**
 * 
 */
package com.automic.azure.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.ByteWriter;
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

    private static final Logger LOGGER = LogManager.getLogger(PutBlockBlobAction.class);

    // size of block blob 4 MB
    private static final int BLOCK_SIZE = 4 * 1024 * 1024;

    // Maximum Blocks
    private static final int MAX_BLOCKS = 50000;

    // min size of blob to be uploaded as a block blob 64 MB
    private static final long FILE_SIZE_FOR_BLOCK_UPLOAD = 64L * 1024 * 1024;

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
    private File blobFile;

    private String contentType;

    /**
     * size of blob file
     */
    private long fileSize;

    private Client storageClient;

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
        initialize();

        validate();

        LOGGER.info("Uploading file of size [" + fileSize + "] bytes.");

        storageClient = storageHttpClient;

        // if file size is greater than 64MB, we upload using put block list.
        if (this.fileSize > FILE_SIZE_FOR_BLOCK_UPLOAD) {
            putBlockList();
        } else {
            putBlock();
        }

        LOGGER.info("Blob [" + blobName + "] has been uploaded succesfully.");
        ConsoleWriter.writeln("UC4RB_AZR_BLOB_NAME ::=" + blobName);
    }

    // upload blob as a single entity
    private void putBlock() {
        WebResource resource = storageClient.resource(storageAccount.blobURL()).path(containerName).path(blobName);

        WebResource.Builder builder = resource.header("Content-Length", fileSize)
                .header("x-ms-version", restapiVersion).header("x-ms-blob-type", "BlockBlob")
                .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());

        ClientResponse response = builder.entity(blobFile, contentType).put(ClientResponse.class);
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("Request ID : " + tokenid.get(0));
    }

    // Upload the blob in chunks
    private void putBlockList() throws AzureException {
        ByteWriter bw = null;
        try {
            // Create the temp file to write the block list information.
            File blockIdListFile = File.createTempFile("BlockList", ".xml");
            LOGGER.info("Temporary Block List File [" + blockIdListFile.getPath() + "] has been created.");
            blockIdListFile.deleteOnExit();

            bw = new ByteWriter(new FileOutputStream(blockIdListFile));
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            bw.write("<BlockList>");

            uploadBlocks(bw);

            bw.write("</BlockList>");
            bw.close();
            commitBlockList(blockIdListFile);
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD, e);
            throw new AzureException(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD);
        } finally {
            // close stream for block Id xml
            if (bw != null) {
                try {
                    bw.close();
                } catch (AzureException e) {
                }
            }
        }
    }

    private void uploadBlocks(ByteWriter bw) throws IOException, AzureException {
        BlockIdGenerator generator = new BlockIdGenerator();
        DateHeader dateHeader = new DateHeader();

        WebResource resource = storageClient.resource(this.storageAccount.blobURL()).path(containerName).path(blobName)
                .queryParam("comp", "block");

        long temp = fileSize;
        long minutes = 0;
        BlockInputStream inputStream = null;
        long start = System.currentTimeMillis();

        try {
            inputStream = new BlockInputStream(blobFile, fileSize);
            while (temp > 0) {
                String blockId = generator.generateBlockIdBase64encoded();
                // add blockid to xml file to commit later
                bw.write("<Uncommitted>");
                bw.write(blockId);
                bw.write("</Uncommitted>");

                long blockSize = BLOCK_SIZE;
                if (temp < BLOCK_SIZE) {
                    blockSize = temp;
                }
                temp = temp - blockSize;
                resource.queryParam("blockid", blockId).header("Content-Length", blockSize)
                        .header("x-ms-version", PutBlockBlobAction.this.restapiVersion)
                        .header("x-ms-blob-type", "BlockBlob").header("x-ms-date", dateHeader.getCurrentDate())
                        .entity(inputStream, contentType).put(ClientResponse.class);

                // Log the information to see the upload progress every minute.
                long elapsedTime = (System.currentTimeMillis() - start) / 1000;
                if ((elapsedTime / 60) > minutes) {
                    minutes++;
                    long avgRate = (fileSize - temp) / elapsedTime;
                    LOGGER.info("Avg. uploading rate (bytes/sec) " + avgRate);
                    LOGGER.info("Remaining bytes to upload " + temp + ". Estimated time(seconds) " + temp / avgRate);
                }
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.closeNow();
                } catch (IOException e) {
                    LOGGER.error(ExceptionConstants.ERROR_BLOCK_BLOB_UPLOAD, e);
                }
            }
        }
    }

    //
    private void commitBlockList(File file) {
        LOGGER.info("Uploading block list to commit");
        WebResource resource = storageClient.resource(this.storageAccount.blobURL()).path(containerName).path(blobName)
                .queryParam("comp", "blocklist");
        WebResource.Builder builder = resource.header("Content-Length", file.length())
                .header("x-ms-version", this.restapiVersion)
                .header("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
        ClientResponse response = builder.entity(file, MediaType.APPLICATION_XML).put(ClientResponse.class);
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("Request ID : " + tokenid.get(0));
    }

    // initialize the parameters
    private void initialize() {
        containerName = getOptionValue(Constants.CONTAINER_NAME);
        blobFile = new File(getOptionValue("blobfile"));
        blobName = getOptionValue("blobname");
        if (!Validator.checkNotEmpty(this.blobName)) {
            this.blobName = blobFile.getName();
        }
        String contentTypeArgs = getOptionValue(Constants.CONTENT_TYPE);
        this.contentType = Validator.checkNotEmpty(contentTypeArgs) ? contentTypeArgs
                : MediaType.APPLICATION_OCTET_STREAM;
    }

    // validate the parameters
    private void validate() throws AzureException {
        // validate storage container name
        if (!Validator.isStorageContainerNameValid(containerName)) {
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
            MediaType.valueOf(contentType);
        } catch (IllegalArgumentException e) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_CONTENT_TYPE);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_CONTENT_TYPE);
        }

        // validate blob name
        if (!Validator.isContainerBlobNameValid(this.blobName)) {
            LOGGER.error(ExceptionConstants.INVALID_BLOB_NAME);
            throw new AzureException(ExceptionConstants.INVALID_BLOB_NAME);
        }

        // validate the max file size
        fileSize = blobFile.length();
        long maxBlobSize = 1L * BLOCK_SIZE * MAX_BLOCKS;
        if (fileSize > maxBlobSize) {
            String msg = String.format(ExceptionConstants.ERROR_BLOB_MAX_SIZE, maxBlobSize, fileSize);
            LOGGER.error(msg);
            throw new AzureException(msg);
        }
    }

    /**
     * Utility class to generate the date header
     */
    private static class DateHeader {
        private final DateFormat rfc1123Format;

        public DateHeader() {
            rfc1123Format = new SimpleDateFormat(Constants.STORAGE_DATE_PATTERN);
            rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        public String getCurrentDate() {
            return rfc1123Format.format(new Date());
        }
    }

    /**
     * Utility class to generate blockid
     */
    private static class BlockIdGenerator {
        private short blockId = 0;
        private ByteBuffer buffer = ByteBuffer.allocate(Short.SIZE);

        public String generateBlockIdBase64encoded() throws UnsupportedEncodingException {
            buffer.putShort(0, blockId++);
            byte[] blockIdBase64 = Base64.encode(buffer.array());
            return new String(blockIdBase64, UT8_ENCODING);
        }
    }

    /**
     * Override the FileInputStream functionality for reading bytes.
     */
    private static class BlockInputStream extends FileInputStream {

        private long size;
        private int temp;

        public BlockInputStream(File f, long sz) throws FileNotFoundException {
            super(f);
            size = sz;
            setMaxRead();
        }

        private void setMaxRead() {
            if (size > BLOCK_SIZE) {
                temp = BLOCK_SIZE;
            } else {
                temp = (int) size;
            }
        }

        public int read() throws IOException {
            throw new IOException("System Error: Implementation not supported");
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int i;

            if (temp == 0) {
                i = -1;
            } else if (temp >= len) {
                i = super.read(b, off, len);
            } else {
                i = super.read(b, off, temp);
            }

            if (i == -1) {
                temp = 0;
            } else {
                temp = temp - i;
                size = size - i;
            }
            return i;
        }

        public void close() {
            setMaxRead();
        }

        public void closeNow() throws IOException {
            super.close();
        }

    }

}
