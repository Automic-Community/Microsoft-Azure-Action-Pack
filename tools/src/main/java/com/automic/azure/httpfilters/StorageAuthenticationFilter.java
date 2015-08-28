/**
 * 
 */
package com.automic.azure.httpfilters;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.util.StorageAuthenticationUtil;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Filter Class to generate Authentication header for Storage service
 *
 */
public class StorageAuthenticationFilter extends ClientFilter {

    private static final Logger LOGGER = LogManager.getLogger(StorageAuthenticationFilter.class);

    /**
     * Storage account
     */
    private AzureStorageAccount storageAccount;

    /**
     * URI for Canonical Resource part of signature
     */
    private String clientURIForSignature;

    /**
     * Common Http headers Example Content-Length
     */
    private Map<String, String> commonHttpHeaders;

    /**
	 * 
	 */
    private List<String> commonHttpHeaderKeys;

    /**
     * 
     */
    private List<String> storageHttpHeaderKeys;

    /**
     * storage Http Headers Example x-ms-version
     */
    private Map<String, String> storageHttpHeaders;

    /**
     * http query parameters Example timeout, restype
     * 
     */
    private Map<String, String> queryParameters;

    /**
     * Initialize Authentication service
     * 
     * @param storageAccount
     * @param isServiceForTable
     *            true if Authenticating for Storage Table service
     * @param commonHeaders
     */
    public StorageAuthenticationFilter(AzureStorageAccount storageAccount, boolean isServiceForTable) {
        this.storageAccount = storageAccount;
        if (isServiceForTable) {

            commonHttpHeaderKeys = StorageAuthenticationFilter.initCommonHeaderKeysForTable();
        } else {

            commonHttpHeaderKeys = StorageAuthenticationFilter.initCommonHeaderKeys();

        }

        storageHttpHeaderKeys = StorageAuthenticationFilter.initStorageHeaderKeysForTable();

        commonHttpHeaders = new HashMap<>();

        storageHttpHeaders = new TreeMap<>();

        queryParameters = new TreeMap<>();

    }

    /**
     * Method to intercept an Http call to storage service and calculate the Authentication header
     */
    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        // set common headers
        setCommonHttpHeaders(request);
        // set storage specific headers
        setStorageHttpHeaders(request);
        // set URI for signature
        setURIforSignature(request);
        // query param
        setQueryParameter(request);
        // create
        String authorizationHeaderValue = null;
        try {
            authorizationHeaderValue = createAuthorizationHeader();
        } catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
            // log and throw any exceptions
            LOGGER.error(ExceptionConstants.ERROR_STORAGE_AUTHENTICATION);
            throw new ClientHandlerException(ExceptionConstants.ERROR_STORAGE_AUTHENTICATION, e);
        }
        request.getHeaders().putSingle("Authorization", authorizationHeaderValue);
        // finally handling the request and returning the response
        return getNext().handle(request);
    }

    /**
     * Set the URI for Canonical Resource part of signature
     * 
     * @param request
     */
    private void setURIforSignature(ClientRequest request) {
        StringBuilder uriForSignature = new StringBuilder();
        uriForSignature.append("/");
        uriForSignature.append(storageAccount.getAccountName());
        uriForSignature.append(request.getURI().getPath());
        this.clientURIForSignature = uriForSignature.toString();

    }

    /**
     * Add common HTTP headers
     * 
     * @param request
     */
    private void setCommonHttpHeaders(ClientRequest request) {
        MultivaluedMap<String, Object> headers = request.getHeaders();
        // parse over headers and get common http headers
        for (Entry<String, List<Object>> entry : headers.entrySet()) {
            if (commonHttpHeaderKeys.contains(entry.getKey())) {
                commonHttpHeaders.put(entry.getKey(), entry.getValue().get(0).toString());
            }
        }
        commonHttpHeaders.put("VERB", request.getMethod());
    }

    /**
     * Add Storage specific HTTP headers
     * 
     * @param request
     */
    private void setStorageHttpHeaders(ClientRequest request) {
        MultivaluedMap<String, Object> headers = request.getHeaders();
        // parse over headers and get common http headers
        for (Entry<String, List<Object>> entry : headers.entrySet()) {
            if (storageHttpHeaderKeys.contains(entry.getKey())) {
                storageHttpHeaders.put(entry.getKey(), entry.getValue().get(0).toString());
            }
        }
    }

    /**
     * Add Query parameters
     * 
     * @param request
     */
    private void setQueryParameter(ClientRequest request) {
        List<NameValuePair> queryParametersList = URLEncodedUtils.parse(request.getURI(), "UTF-8");
        for (NameValuePair queryParameter : queryParametersList) {
            queryParameters.put(queryParameter.getName(), queryParameter.getValue());
        }

    }

    // common header keys for Blob, File service
    private static List<String> initCommonHeaderKeys() {
        List<String> commonHeaderKeys = new ArrayList<>();
        commonHeaderKeys.add("VERB");
        commonHeaderKeys.add("Content-Encoding");
        commonHeaderKeys.add("Content-Language");
        commonHeaderKeys.add("Content-Length");
        commonHeaderKeys.add("Content-MD5");
        commonHeaderKeys.add("Content-Type");
        commonHeaderKeys.add("Date");
        commonHeaderKeys.add("If-Modified-Since");
        commonHeaderKeys.add("If-Match");
        commonHeaderKeys.add("If-None-Match");
        commonHeaderKeys.add("If-Unmodified-Since");
        commonHeaderKeys.add("Range");

        return commonHeaderKeys;
    }

    // common header keys for Table service
    private static List<String> initCommonHeaderKeysForTable() {
        List<String> commonHeaderKeys = new ArrayList<>();
        commonHeaderKeys.add("VERB");
        commonHeaderKeys.add("Content-MD5");
        commonHeaderKeys.add("Content-Type");
        commonHeaderKeys.add("Date");

        return commonHeaderKeys;
    }

    // Storage specific header keys for Table service
    private static List<String> initStorageHeaderKeysForTable() {
        List<String> storageHeaderKeys = new ArrayList<>();
        storageHeaderKeys.add("x-ms-version");
        storageHeaderKeys.add("x-ms-date");
        storageHeaderKeys.add("x-ms-blob-public-access");

        return storageHeaderKeys;
    }

    /**
     * Method to create authorization header String
     * 
     * @return Value for Authorization header
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws AzureException
     */
    private String createAuthorizationHeader() throws InvalidKeyException, UnsupportedEncodingException,
            NoSuchAlgorithmException {

        StringBuilder authorizationHeader = new StringBuilder();
        authorizationHeader.append("SharedKey ");
        authorizationHeader.append(this.storageAccount.getAccountName());
        authorizationHeader.append(":");
        // signature
        authorizationHeader.append(new String(createAuthorizationSignature(), "UTF-8"));
        return authorizationHeader.toString();
    }

    // Method to create authorization Signature and encode with HMACSHA256
    // algorithm
    private byte[] createAuthorizationSignature() throws InvalidKeyException, UnsupportedEncodingException,
            NoSuchAlgorithmException {

        StringBuilder authorizationSignature = new StringBuilder();
        // create Signature
        authorizationSignature.append(createAuthorizationCommonSignature());

        authorizationSignature.append(createCannonicalHeaders());

        authorizationSignature.append(createCannonicalResource());

        LOGGER.info("generated Signature for request");
        LOGGER.info(authorizationSignature);
        // encode Signature with
        return StorageAuthenticationUtil.generateHMACSHA256WithKey(authorizationSignature.toString(),
                this.storageAccount.getPrimaryAccessKey());

    }

    // create common part of signature
    private String createAuthorizationCommonSignature() {
        StringBuilder commonSignature = new StringBuilder();
        // common http headers
        for (String headerKey : commonHttpHeaderKeys) {
            String value = commonHttpHeaders.get(headerKey);
            commonSignature.append(value != null ? value : Strings.EMPTY);
            commonSignature.append("\n");
        }
        return commonSignature.toString();
    }

    // create cannonical header
    private String createCannonicalHeaders() {
        StringBuilder cannonicalHeader = new StringBuilder();
        // storage specific http headers
        for (String headerKey : storageHttpHeaders.keySet()) {
            cannonicalHeader.append(headerKey).append(":").append(storageHttpHeaders.get(headerKey));
            cannonicalHeader.append("\n");
        }
        return cannonicalHeader.toString();
    }

    // create cannonical header
    private String createCannonicalResource() {
        StringBuilder cannonicalResource = new StringBuilder();
        cannonicalResource.append(this.clientURIForSignature);
        cannonicalResource.append("\n");
        // storage query parameters
        for (Iterator<String> iterator = queryParameters.keySet().iterator(); iterator.hasNext();) {
            String headerKey = iterator.next();
            cannonicalResource.append(headerKey).append(":").append(queryParameters.get(headerKey));
            if (iterator.hasNext()) {
                cannonicalResource.append("\n");
            }

        }
        return cannonicalResource.toString();
    }

}
