/**
 * 
 */
package com.automic.azure.httpfilters;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	 * 
	 */
    private List<String> commonHttpHeaderKeys = StorageAuthenticationFilter.initCommonHeaderKeys();

    
    /**
     * Initialize Authentication service
     * 
     * @param storageAccount
     * @param isServiceForTable
     *            true if Authenticating for Storage Table service
     * @param commonHeaders
     */
    public StorageAuthenticationFilter(AzureStorageAccount storageAccount) {
        this.storageAccount = storageAccount;

    }

    /**
     * Method to intercept an Http call to storage service and calculate the Authentication header
     */
    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        
        // create authorization header
        String authorizationHeaderValue = null;
        try {
            authorizationHeaderValue = createAuthorizationHeader(request);
        } catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
            // log and throw any exceptions
            LOGGER.error(ExceptionConstants.ERROR_STORAGE_AUTHENTICATION);
            throw new ClientHandlerException(ExceptionConstants.ERROR_STORAGE_AUTHENTICATION, e);
        }
        request.getHeaders().putSingle("Authorization", authorizationHeaderValue);
        // finally handling the request and returning the response
        return getNext().handle(request);
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

    /**
     * Method to create authorization header String 
     * 
     * @return Value for Authorization header
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws AzureException
     */
    private String createAuthorizationHeader(ClientRequest request) throws InvalidKeyException,
            UnsupportedEncodingException, NoSuchAlgorithmException {

        StringBuilder authorizationHeader = new StringBuilder();
        authorizationHeader.append("SharedKey ");
        authorizationHeader.append(this.storageAccount.getAccountName());
        authorizationHeader.append(":");
        // signature
        authorizationHeader.append(new String(createAuthorizationSignature(request), "UTF-8"));
        return authorizationHeader.toString();
    }

    // Method to create authorization Signature and encode with HMACSHA256
    // algorithm
    private byte[] createAuthorizationSignature(ClientRequest request) throws InvalidKeyException,
            UnsupportedEncodingException, NoSuchAlgorithmException {

        StringBuilder authorizationSignature = new StringBuilder();
        // create Signature
        authorizationSignature.append(createAuthorizationCommonSignature(request));

        authorizationSignature.append(createCannonicalHeaders(request));

        authorizationSignature.append(createCannonicalResource(request));

        LOGGER.info("generated Signature for request");
        LOGGER.info(authorizationSignature);
        // encode Signature with
        return StorageAuthenticationUtil.generateHMACSHA256WithKey(authorizationSignature.toString(),
                this.storageAccount.getPrimaryAccessKey());

    }

    // create common part of signature
    private String createAuthorizationCommonSignature(ClientRequest request) {
        StringBuilder commonSignature = new StringBuilder();
        MultivaluedMap<String, Object> headers = request.getHeaders();

        // common http headers
        for (String headerKey : commonHttpHeaderKeys) {
            if ("VERB".equals(headerKey)) {
                commonSignature.append(request.getMethod());
                commonSignature.append("\n");
            } else {

                Object value = headers.get(headerKey) != null ? headers.get(headerKey).get(0) : null;
                commonSignature.append(value != null ? value : Strings.EMPTY);
                commonSignature.append("\n");
            }
        }
        return commonSignature.toString();
    }

    // create cannonical header
    private String createCannonicalHeaders(ClientRequest request) {
        StringBuilder cannonicalHeader = new StringBuilder();
        Map<String, List<Object>> headers = new TreeMap<>(request.getHeaders());
        // storage specific http headers
        for (Entry<String, List<Object>> headerEntry : headers.entrySet()) {
            // if it is a storage header
            if (headerEntry.getKey().startsWith("x-ms")) {
                cannonicalHeader.append(headerEntry.getKey()).append(":").append(headerEntry.getValue().get(0));
                cannonicalHeader.append("\n");
            }
        }
        return cannonicalHeader.toString();
    }

    // create cannonical Resource
    private String createCannonicalResource(ClientRequest request) {
        StringBuilder cannonicalResource = new StringBuilder();
        // set URI in cannonical Resource
        cannonicalResource.append(getURIforSignature(request));
        cannonicalResource.append("\n");
        List<NameValuePair> queryParametersList = URLEncodedUtils.parse(request.getURI(), "UTF-8");
        
        // sort the query parameters in lexicological order
        Collections.sort(queryParametersList, new Comparator<NameValuePair>() {

            @Override
            public int compare(NameValuePair o1, NameValuePair o2) {
                // 
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        // storage query parameters
        for (Iterator<NameValuePair> iterator = queryParametersList.iterator(); iterator.hasNext();) {
            NameValuePair queryParam = iterator.next();
            cannonicalResource.append(queryParam.getName()).append(":").append(queryParam.getValue());
            if (iterator.hasNext()) {
                cannonicalResource.append("\n");
            }

        }
        return cannonicalResource.toString();
    }

    // Method to get the URI for Canonical Resource part of signature
    private String getURIforSignature(ClientRequest request) {
        StringBuilder uriForSignature = new StringBuilder();
        uriForSignature.append("/");
        uriForSignature.append(storageAccount.getAccountName());
        uriForSignature.append(request.getURI().getPath());
        return uriForSignature.toString();
    }

}
