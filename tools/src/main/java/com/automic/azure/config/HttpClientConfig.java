package com.automic.azure.config;

import com.automic.azure.exceptions.AzureException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public final class HttpClientConfig {

	private HttpClientConfig() {
	}

	public static Client getClient(String keyStore, String password, int connectionTimeOut, int readTimeOut)
			throws AzureException {
		Client client;

		ClientConfig config = new DefaultClientConfig();

		config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeOut);
		config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeOut);

		AzureCertificatesManagement acm = new AzureCertificatesManagement(keyStore, password);

		HTTPSProperties props = new HTTPSProperties(acm.hostnameVerifier(), acm.getSslContext());
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, props);

		client = Client.create(config);

		return client;
	}

}
