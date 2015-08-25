/**
 * 
 */
package com.automic.azure.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.util.StorageAuthenticationUtil;

/**
 * Class as a service to generate Authentication for Storage service 
 *
 */
public class AzureStorageAuthenticationService {

	private static final Logger LOGGER = LogManager
			.getLogger(AzureStorageAuthenticationService.class);

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
	public AzureStorageAuthenticationService(
			AzureStorageAccount storageAccount, boolean isServiceForTable) {
		this.storageAccount = storageAccount;
		this.clientURIForSignature = "/" + storageAccount.getAccountName();

		if (isServiceForTable) {

			commonHttpHeaderKeys = AzureStorageAuthenticationService
					.initCommonHeaderKeysForTable();
		} else {

			commonHttpHeaderKeys = AzureStorageAuthenticationService
					.initCommonHeaderKeys();

		}

		commonHttpHeaders = new HashMap<>();

		storageHttpHeaders = new TreeMap<>();

		queryParameters = new TreeMap<>();

	}

	/**
	 * Set the URI for Canonical Resource part of signature
	 * @param clientURIForSignature
	 */
	public void setURIforSignature(String clientURIForSignature) {
		this.clientURIForSignature = clientURIForSignature;

	}

	/**
	 * Add common HTTP headers
	 * @param key
	 * @param value
	 */
	public void addCommonHttpHeaders(String key, String value) {
		commonHttpHeaders.put(key, value);
	}

	/**
	 * Add Storage specific HTTP headers
	 * @param key
	 * @param value
	 */
	public void addStorageHttpHeaders(String key, String value) {
		storageHttpHeaders.put(key, value);
	}

	/**
	 *  Add Query parameters 
	 * @param key
	 * @param value
	 */
	public void addQueryParameter(String key, String value) {
		queryParameters.put(key, value);
	}

	/**
	 * Get Storage specific HTTP headers
	 * @return
	 */
	public Map<String, String> getStorageHttpHeaders() {
		return storageHttpHeaders;
	}

	/**
	 * Get Query parameters 
	 * @return
	 */
	public Map<String, String> getQueryParameters() {
		return queryParameters;
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

	//common header keys for Table service
	private static List<String> initCommonHeaderKeysForTable() {
		List<String> commonHeaderKeys = new ArrayList<>();
		commonHeaderKeys.add("VERB");
		commonHeaderKeys.add("Content-MD5");
		commonHeaderKeys.add("Content-Type");
		commonHeaderKeys.add("Date");

		return commonHeaderKeys;
	}

	/**
	 * Method to create authorization header String
	 * 
	 * @return Value for Authorization header
	 * @throws AzureException
	 */
	public String createAuthorizationHeader() throws AzureException {

		StringBuilder authorizationHeader = new StringBuilder();
		authorizationHeader.append("SharedKey ");
		authorizationHeader.append(this.storageAccount.getAccountName());
		authorizationHeader.append(":");
		// signature
		authorizationHeader.append(new String(createAuthorizationSignature()));
		return authorizationHeader.toString();
	}

	// Method to create authorization Signature and encode with HMACSHA256
	// algorithm
	private byte[] createAuthorizationSignature() throws AzureException {

		StringBuilder authorizationSignature = new StringBuilder();
		// create Signature
		authorizationSignature.append(createAuthorizationCommonSignature());
		
		authorizationSignature.append(createCannonicalHeaders());
		
		authorizationSignature.append(createCannonicalResource());

		LOGGER.info("generated Signature for request");
		LOGGER.info(authorizationSignature);
		// encode Signature with
		try {
			return StorageAuthenticationUtil.generateHMACSHA256WithKey(
					authorizationSignature.toString(),
					this.storageAccount.getPrimaryAccessKey());
		} catch (InvalidKeyException | UnsupportedEncodingException
				| NoSuchAlgorithmException e) {
			LOGGER.error(ExceptionConstants.ERROR_STORAGE_AUTHENTICATION, e);
			throw new AzureException(
					ExceptionConstants.ERROR_STORAGE_AUTHENTICATION, e);
		}
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
			cannonicalHeader.append(headerKey).append(":")
			.append(storageHttpHeaders.get(headerKey));
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
		for (Iterator<String> iterator = queryParameters.keySet().iterator(); iterator
				.hasNext();) {
			String headerKey = iterator.next();
			cannonicalResource.append(headerKey).append(":")
					.append(queryParameters.get(headerKey));
			if (iterator.hasNext()) {
				cannonicalResource.append("\n");
			}

		}
		return cannonicalResource.toString();
	}

}
