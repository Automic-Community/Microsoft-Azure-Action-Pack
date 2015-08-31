package com.automic.azure.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.automic.azure.filter.StorageAuthenticationUtil.QueryParameter;
import com.automic.azure.model.AzureStorageAccount;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Filter Class to generate Authentication header for Storage service
 *
 */
public class StorageAuthenticationFilter extends ClientFilter {

    private AzureStorageAccount storageAccount;

    /**
     * Initialize Authentication service
     * 
     * @param storageAccount
     */
    public StorageAuthenticationFilter(AzureStorageAccount storageAccount) {
        this.storageAccount = storageAccount;

    }

    /**
     * Method to intercept the client request to add the Authorization header. The format for the Authorization header
     * is as follows Authorization="SharedKey <AccountName>:<Signature>"
     */
    @Override
    public ClientResponse handle(ClientRequest request) {

        StringBuilder header = new StringBuilder();

        header.append("SharedKey ");
        header.append(storageAccount.getAccountName()).append(":");
        // sign the signature string with the provided access key.
        header.append(StorageAuthenticationUtil.signHmacSHA256(createSignatureString(request),
                storageAccount.getPrimaryAccessKey()));
        request.getHeaders().putSingle("Authorization", header.toString());

        return getNext().handle(request);
    }

    /**
     * Creates the Signature String in the following format. StringToSign = VERB + "\n" + Content-Encoding + "\n" +
     * Content-Language + "\n" + Content-Length + "\n" + Content-MD5 + "\n" + Content-Type + "\n" + Date + "\n" +
     * If-Modified-Since + "\n" + If-Match + "\n" + If-None-Match + "\n" + If-Unmodified-Since + "\n" + Range + "\n" +
     * CanonicalizedHeaders + CanonicalizedResource;
     */

    private String createSignatureString(ClientRequest cr) {
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(cr.getMethod()).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "Content-Encoding")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "Content-Language")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "Content-Length")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "Content-MD5")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "Content-Type")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "Date")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "If-Modified-Since")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "If-Match")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "If-None-Match")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "If-Unmodified-Since")).append("\n")
                .append(StorageAuthenticationUtil.getHeader(cr, "Range")).append("\n");

        stringToSign.append(getCanonicalizedHeaders(cr));
        stringToSign.append(getCanonicalizedResource(cr));

        return stringToSign.toString();
    }

    /**
     * Construct the Canonicalized Headers as follows 1. Retrieve all headers for the resource that begin with x-ms-,
     * including the x-ms-date header. 2. Convert each HTTP header name to lowercase. 3. Sort the headers
     * lexicographically by header name, in ascending order. 4. Unfold the string by replacing any breaking white space
     * with a single space. 5. Trim any white space around the colon in the header. 6. Finally, append a new-line
     * character to each canonicalized header in the resulting list. 7. Construct the CanonicalizedHeaders string by
     * concatenating all headers in this list into a single string.
     */
    private String getCanonicalizedHeaders(ClientRequest cr) {
        List<String> msHeaders = new ArrayList<String>();
        for (String key : cr.getHeaders().keySet()) {
            String lowerCase = key.toLowerCase(Locale.US);
            if (lowerCase.startsWith("x-ms-")) {
                msHeaders.add(lowerCase);
            }
        }
        Collections.sort(msHeaders);

        StringBuilder result = new StringBuilder();
        for (String msHeader : msHeaders) {
            result.append(msHeader).append(":").append(cr.getHeaders().getFirst(msHeader)).append("\n");
        }
        return result.toString();
    }

    /**
     * Construct the Canonicalized Resource as follows 1. Beginning with an empty string (""), append a forward slash
     * (/), followed by the name of the account that owns the resource being accessed. 2. Append the resource's encoded
     * URI path, without any query parameters. 3. Append a new-line character (\n) after the resource name. 4. Retrieve
     * all query parameters on the resource URI, including the comp parameter if it exists. 5. Convert all parameter
     * names to lowercase. 6. Sort the query parameters lexicographically by parameter name, in ascending order. 7.
     * URL-decode each query parameter name and value. 8. Append each query parameter name and value to the string in
     * the following format, making sure to include the colon (:) between the name and the value:
     * parameter-name:parameter-value 9. If a query parameter has more than one value, sort all values
     * lexicographically, then include them in a comma-separated list:
     * parameter-name:parameter-value-1,parameter-value-2,parameter-value-n 10. Append a new-line character (\n) after
     * each name-value pair.
     *
     * Keep in mind the following rules for constructing the canonicalized resource string: 1. Avoid using the new-line
     * character (\n) in values for query parameters. If it must be used, ensure that it does not affect the format of
     * the canonicalized resource string. 2. Avoid using commas in query parameter values.
     */

    private String getCanonicalizedResource(ClientRequest cr) {
        StringBuilder result = new StringBuilder();

        result.append("/").append(storageAccount.getAccountName());
        result.append(cr.getURI().getPath());

        List<QueryParameter> queryParams = StorageAuthenticationUtil.getQueryParameters(cr.getURI());
        for (QueryParameter queryParam : queryParams) {
            queryParam.setName(queryParam.getName().toLowerCase(Locale.US));
        }

        Collections.sort(queryParams);

        for (int i = 0; i < queryParams.size(); i++) {
            QueryParameter param = queryParams.get(i);

            result.append("\n").append(param.getName()).append(":");

            List<String> values = param.getValues();
            Collections.sort(values);
            for (int j = 0; j < values.size(); j++) {
                if (j > 0) {
                    result.append(",");
                }
                result.append(values.get(j));
            }
        }
        return result.toString();
    }

}
