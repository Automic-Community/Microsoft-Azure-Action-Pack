/**
 * 
 */
package com.automic.azure.config;

import java.nio.file.Path;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.exceptions.AzureException;


/**
 * DockerCertificates holds certificates for connecting to an HTTPS-secured Docker instance with client/server
 * authentication.
 */
public class CertificatesManagement {
    private static final Logger LOGGER = LogManager.getLogger(CertificatesManagement.class);


    private SSLContext sslContext;

    public CertificatesManagement(final Path keyStore) throws AzureException {

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


