package com.automic.azure.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.exceptions.AzureException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * Configuration class for http client.This class sets the parameters like connection timeout , read time out , handling
 * of cookies and whether to retry or not
 * */
public final class HttpClientConfig {

    private static final Logger LOGGER = LogManager.getLogger(HttpClientConfig.class);

    private HttpClientConfig() {
    }

    /**
     * Creates an instance of {@link Client} using parameters specified.  
     * @param 
     * @param 
     * @param connectionTimeOut timeout in milliseconds
     * @param readTimeOut read timeout in milliseconds
     * @return an instance of {@link Client}
     * @throws AzureException
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyStoreException 
     * @throws KeyManagementException 
     * @throws UnrecoverableKeyException 
     */
     public static Client getClient(String keyStore,String password, int connectionTimeOut, int readTimeOut)
            throws AzureException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, IOException {
        Client client;

        ClientConfig config = new DefaultClientConfig();

       
        config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeOut);
        SSLSocketFactory sslFactory = getSSLSocketFactory(keyStore,	password);
        
        
        client = Client.create(config);

        return client;
    }
    
    
    private static SSLSocketFactory getSSLSocketFactory(String keyStoreName,
			String password) throws UnrecoverableKeyException,
			KeyStoreException, NoSuchAlgorithmException,
			KeyManagementException, IOException {
		KeyStore ks = getKeyStore(keyStoreName, password);
		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance("SunX509");
		keyManagerFactory.init(ks, password.toCharArray());

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(keyManagerFactory.getKeyManagers(), null,
				new SecureRandom());

		return context.getSocketFactory();
	}
    
    
    private static KeyStore getKeyStore(String keyStoreName, String password)
			throws IOException {
		KeyStore ks = null;
		FileInputStream fis = null;
		try {
			ks = KeyStore.getInstance("JKS");
			char[] passwordArray = password.toCharArray();
			fis = new java.io.FileInputStream(keyStoreName);
			ks.load(fis, passwordArray);
			fis.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return ks;
	}


}
