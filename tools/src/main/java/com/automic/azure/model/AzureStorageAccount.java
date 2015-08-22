package com.automic.azure.model;


/**
 * 
 * Class that represents a Storage account in Azure
 *
 */
public class AzureStorageAccount {
	
	private static final String AZURE_STORAGE_HOST = "core.windows.net";
	
	
	
	/**
	 * Storage account name
	 */
	private String accountName;
	
	/**
	 * primary access key to sign Singnature for Authentication
	 */
	private String primaryAccessKey;
	
	
	/**
	 * @param accountName
	 * @param primaryAccessKey
	 */
	public AzureStorageAccount(String accountName, String primaryAccessKey) {
		super();
		this.accountName = accountName;
		this.primaryAccessKey = primaryAccessKey;
	}

	
	/**
	 * get Account name
	 * @return
	 */
	public String getAccountName() {
		return accountName;
	}


	/**
	 * get Primary Access Key
	 * @return
	 */
	public String getPrimaryAccessKey() {
		return primaryAccessKey;
	}


	/**
	 * get URL for Blob service  
	 * @return
	 */
	public  String blobURL(){
		return "https://" + accountName + ".blob." + AZURE_STORAGE_HOST;
	}
	
	
	/**
	 * get URL for table service
	 * @return
	 */
	public  String tableURL(){
		return "https://" + accountName + ".table." + AZURE_STORAGE_HOST;
	}
	
	/**
	 * get URL for File service
	 * @return
	 */
	public  String fileURL(){
		return "https://" + accountName + ".file." + AZURE_STORAGE_HOST;
	}

	
	
}
