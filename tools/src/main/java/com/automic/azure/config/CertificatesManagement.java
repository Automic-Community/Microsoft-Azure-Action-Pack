/**
 * 
 */
package com.automic.azure.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;



/**
 * DockerCertificates holds certificates for connecting to an HTTPS-secured Docker instance with client/server
 * authentication.
 */
public class CertificatesManagement {
    private static final Logger LOGGER = LogManager.getLogger(CertificatesManagement.class);


    private SSLContext sslContext;

    public CertificatesManagement(final Path keyStore, char [] password) throws AzureException {
    	
    	File keyStorefile = keyStore.toFile();
    	
    	 if (!keyStorefile.exists() || !keyStorefile.exists() || !keyStorefile.exists()) {
             throw new AzureException(ExceptionConstants.AZURE_CERTIFICATE_MISSING);
         }
    	
    	 prepareSSl(keyStore, password);

    }
    
    private void prepareSSl(final Path keyStorePath, char [] keyStorePassword) throws AzureException {   	
    
        try { 
            KeyStore ks = getKeyStore(keyStorePath, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(ks, keyStorePassword);     
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            LOGGER.error("Error in preparing https certificates ", e);
            throw new AzureException(ExceptionConstants.AZURE_CERTIFICATE_MISSING);
        }     	
    	
    }
    
    private  KeyStore getKeyStore(Path keyStoreName, char []  password) throws IOException
    {
        KeyStore ks = null;
        try (FileInputStream fis = new java.io.FileInputStream(keyStoreName.toFile())){
            ks = KeyStore.getInstance("JKS");
            ks.load(fis, password);
            fis.close();             
        } catch (Exception e) {        	
        	LOGGER.error("Error in reading/loading jks certificates ", e);
        }       
        return ks;
    }

    /**
     * Method to get the instance of {@link SSLContext} for Docker connection
     * @return
     */
    public SSLContext sslContext() {

        return this.sslContext;
    }

    /**
     * Method to get the instance of {@link HostnameVerifier}  
     * @return
     */
    public HostnameVerifier hostnameVerifier() {
        return SSLConnectionSocketFactory.getDefaultHostnameVerifier();
    }

}


