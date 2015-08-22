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

import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.util.StorageAuthenticationUtil;

/**
 * Abstract Class as a service to interact with Azure Storage Service
 *
 */
public abstract class AzureStorageService {

	
	private static final Logger LOGGER = LogManager.getLogger(AzureStorageService.class);
	
	
	/**
	 * Storage account
	 */
	protected AzureStorageAccount storageAccount;
	
	
	protected String clientURIForSignature;

	/**
	 * Common Http headers Example Content-Length
	 */
	protected Map<String, String> commonHttpHeaders;
	
	/**
	 * 
	 */
	protected List<String> commonHttpHeaderKeys;

	/**
	 * storage Http Headers Example x-ms-version
	 */
	protected TreeMap<String, String> storageHttpHeaders;

	/**
	 * http query parameters Example timeout, restype
	 * 
	 */
	protected TreeMap<String, String> queryParameters;

	/**
	 * 
	 * @param storageAccount
	 * @param commonHeaders
	 */
	public AzureStorageService(AzureStorageAccount storageAccount,
			Map<String, String> commonHeaders, boolean isServiceForTable) {
		this.storageAccount = storageAccount;
		this.clientURIForSignature = "/" + storageAccount.getAccountName();
		
		if(isServiceForTable){
			commonHttpHeaders = AzureStorageService.initCommonHeadersMapForTable(commonHeaders);
		}else{
			commonHttpHeaders = AzureStorageService.initCommonHeadersMap(commonHeaders);
		}
		
		commonHttpHeaderKeys = AzureStorageService.initCommonHeaderKeys();
		
		storageHttpHeaders = new TreeMap<>();

		queryParameters = new TreeMap<>();

	}
	
	

	public void addStorageHttpHeaders(String key, String value){
		storageHttpHeaders.put(key, value);
	}
	
	
	public void addQueryParameter(String key, String value){
		queryParameters.put(key, value);
	}

	
	/**
	 * initialize Commomn Headers Map for Blob, File service
	 * @param commonHeaders
	 * @return
	 */
	private static Map<String, String> initCommonHeadersMap(
			Map<String, String> commonHeaders) {
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("VERB", commonHeaders.get("VERB"));
		headerMap.put("Content-Encoding", commonHeaders.get("Content-Encoding"));
		headerMap.put("Content-Language", commonHeaders.get("Content-Language"));
		headerMap.put("Content-Length", commonHeaders.get("Content-Length"));
		headerMap.put("Content-MD5", commonHeaders.get("Content-MD5"));
		headerMap.put("Content-Type", commonHeaders.get("Content-Type"));
		headerMap.put("Date", commonHeaders.get("Date"));
		headerMap.put("If-Modified-Since", commonHeaders.get("If-Modified-Since"));
		headerMap.put("If-Match", commonHeaders.get("If-Match"));
		headerMap.put("If-None-Match", commonHeaders.get("If-None-Match"));
		headerMap.put("If-Unmodified-Since", commonHeaders.get("If-Unmodified-Since"));
		headerMap.put("Range", commonHeaders.get("Range"));
		return headerMap;

	}
	
	// 
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
	 * initialize Commomn Headers Map for Table service
	 * @param commonHeaders
	 * @return
	 */
	public static Map<String, String> initCommonHeadersMapForTable(
			Map<String, String> commonHeaders) {
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("VERB", commonHeaders.get("VERB"));

		headerMap.put("Content-MD5", commonHeaders.get("Content-MD5"));
		headerMap.put("Content-Type", commonHeaders.get("Content-Type"));
		headerMap.put("Date", commonHeaders.get("Date"));

		return headerMap;

	}


	
	// Method to create authorization header String
	protected String createAuthorizationHeader() throws AzureException{
		
		StringBuilder authorizationHeader = new StringBuilder();
		authorizationHeader.append("SharedKey ");
		authorizationHeader.append(this.storageAccount.getAccountName());
		authorizationHeader.append(":");
		// signature
		authorizationHeader.append(new String(createAuthorizationSignature()));
		return authorizationHeader.toString();
	}
	
	
	
	// Method to create authorization Signature and encode with HMACSHA256 algorithm
	private byte[] createAuthorizationSignature() throws AzureException{
		
		StringBuilder authorizationSignature = new StringBuilder();
		// create Signature
		authorizationSignature.append(createAuthorizationCommonSignature());
		//authorizationSignature.append("\n");
		authorizationSignature.append(createCannonicalHeaders());
		//authorizationSignature.append("\n");
		authorizationSignature.append(createCannonicalResource());
		
		LOGGER.info("generated Signature for request");
		LOGGER.info(authorizationSignature);
		// encode Signature with 
		try {
			return StorageAuthenticationUtil.generateHMACSHA256WithKey(authorizationSignature.toString(), this.storageAccount.getPrimaryAccessKey());
		} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
			LOGGER.error("Error in Creating Authentication Signature for Storage Service", e);
			throw new AzureException("Error in Creating Authentication Signature for Storage Service", e);
		} 
	}
	
	
	// create common part of signature
	private String createAuthorizationCommonSignature(){
		StringBuilder commonSignature = new StringBuilder();
		// common http headers
		for(String headerKey : commonHttpHeaderKeys){
			String value = commonHttpHeaders.get(headerKey);
			commonSignature.append(value != null ? value : Strings.EMPTY);
			commonSignature.append("\n");
		}
		return commonSignature.toString();
	}
	
	
	// create cannonical header
	private String createCannonicalHeaders(){
		StringBuilder cannonicalHeader = new StringBuilder();
		// storage specific http headers
		for(String headerKey : storageHttpHeaders.keySet()){
			cannonicalHeader.append(headerKey + ":" + storageHttpHeaders.get(headerKey));
			cannonicalHeader.append("\n");
		}
		return cannonicalHeader.toString();
	}
	
	
	// create cannonical header
	private String createCannonicalResource(){
		StringBuilder cannonicalResource = new StringBuilder();
		cannonicalResource.append(this.clientURIForSignature);
		cannonicalResource.append("\n");
		// storage query parameters
		for(Iterator<String> iterator =  queryParameters.keySet().iterator() ; iterator.hasNext(); ) {
			String headerKey = iterator.next();
			cannonicalResource.append(headerKey + ":" + queryParameters.get(headerKey));
			if(iterator.hasNext()){
				cannonicalResource.append("\n");
			}
			
		}
		return cannonicalResource.toString();
	}
	
	
	
	
	
	
	
	
	

}
