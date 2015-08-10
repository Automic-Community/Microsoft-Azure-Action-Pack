/**
 * 
 */
package com.automic.azure.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;


/**
 * AzureCertificatesManagement holds certificates for connecting to an HTTPS-secured Azure instance with client/server
 * authentication.
 */
public class AzureCertificatesManagement {
  

	private static final Logger LOGGER = LogManager.getLogger(AzureCertificatesManagement.class);
    private SSLContext sslContext;
    
    AzureCertificatesManagement(String keyStoreLoc ,String password) throws AzureException{
    	try {
			this.sslContext = setSSLSocketContext(keyStoreLoc,password);
		} catch (UnrecoverableKeyException |KeyManagementException |KeyStoreException |NoSuchAlgorithmException | IOException e) {
			LOGGER.error("Error during sslcontext creation ",e);
			throw new AzureException(ExceptionConstants.GENERIC_ERROR_MSG,e);
		} 
    }

     
    private static SSLContext setSSLSocketContext(String keyStoreName,
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

		return context;
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

		} catch (KeyStoreException |NoSuchAlgorithmException | CertificateException e) {
			
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return ks;
	}
   
    /**
     * Method to get the instance of {@link SSLContext} for Azure connection
     * @return
     */
    public SSLContext getSslContext() {
  		return sslContext;
  	}

    /**
     * Method to get the instance of {@link HostnameVerifier}  
     * @return
     */
    public HostnameVerifier hostnameVerifier() {
        return SSLConnectionSocketFactory.getDefaultHostnameVerifier();
    }
    
    

}


