package com.automic.azure.filter;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.sun.jersey.api.client.ClientRequest;

/**
 * Utility Class to generate authentication
 * 
 *
 */
public final class StorageAuthenticationUtil {

    private StorageAuthenticationUtil() {
    }

    /**
     * Get the header value for the given header name inside specified request. It returns empty string, if header is
     * not present or more than 1 values found for the specified header.
     * 
     * @param cr
     *            ClientRequest
     * @param headerKey
     * @return header value or empty string
     */
    public static String getHeader(ClientRequest cr, String headerKey) {
        List<Object> values = cr.getHeaders().get(headerKey);
        if (values == null || values.size() != 1) {
            return emptyOnNull(null);
        }
        return emptyOnNull(values.get(0).toString());
    }

    private static String emptyOnNull(String value) {
        return value != null ? value : "";
    }

    /**
     * Get the query parameters for the specified URI. Every element in the list will further contain parameter name and
     * corresponding list of values.
     * 
     * @param uri
     *            represents java.net.URI
     * @return List of QueryParameters
     */
    public static List<QueryParameter> getQueryParameters(URI uri) {
        List<NameValuePair> queryParametersList = URLEncodedUtils.parse(uri, "UTF-8");
        List<QueryParameter> result = new ArrayList<QueryParameter>();
        for (NameValuePair param : queryParametersList) {
            result.add(getQueryParameter(param.getName(), param.getValue()));
        }
        return result;
    }

    private static QueryParameter getQueryParameter(String paramName, String paramValue) {
        QueryParameter result = new QueryParameter();

        result.setName(paramName);
        int index = paramValue.indexOf(',');
        if (index < 0) {
            result.addValue(paramValue);
        } else {
            for (String v : paramValue.split(",")) {
                result.addValue(v);
            }
        }

        return result;
    }

    /**
     * Represents the QueryParameter class. It is a value object which hold the query parameter information.
     */

    public static class QueryParameter implements Comparable<QueryParameter> {
        private String name;
        private final List<String> values = new ArrayList<String>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getValues() {
            return values;
        }

        public void addValue(String value) {
            values.add(value);
        }

        public int compareTo(QueryParameter o) {
            return this.name.compareTo(o.name);
        }
    }

    /**
     * 
     * @param stringToSign
     * @param accessKey
     * @return signed string
     * @throws IllegalArgumentException
     */
    public static String signHmacSHA256(String stringToSign, String accessKey) {

        try {
            // generate signing key
            SecretKeySpec signingKey = new SecretKeySpec(Base64.decodeBase64(accessKey), "hmacSHA256");

            // initialize MAC with signing key
            Mac hmac = Mac.getInstance("hmacSHA256");
            hmac.init(signingKey);

            // sign header with MAC
            byte[] digest = hmac.doFinal(stringToSign.getBytes("UTF-8"));

            // encode signature with base64
            return new String(Base64.encodeBase64(digest), "UTF-8");

        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

}
