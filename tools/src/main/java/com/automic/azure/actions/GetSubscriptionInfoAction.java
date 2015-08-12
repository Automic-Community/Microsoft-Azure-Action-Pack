/**
 * 
 */
package com.automic.azure.actions;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.utility.CommonUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to fetch Subscription details from Azure for provided
 * Subscription info. Following information is required to fetch subscription
 * details from Azure system:
 * <ul>
 * <li>Subscription Id</li>
 * <li>Authentication Certificate</li>
 * <li>Certificate password</li>
 * </ul>
 *
 */
public class GetSubscriptionInfoAction extends AbstractAction {

	private static final Logger LOGGER = LogManager
			.getLogger(GetSubscriptionInfoAction.class);

	@Override
	protected Options initializeOptions() {
		return actionOptions;
	}

	@Override
	protected void logParameters(Map<String, String> argumentMap) {
		LOGGER.info(argumentMap);

	}

	@Override
	protected void initialize(Map<String, String> argumentMap) {

	}

	@Override
	protected void validateInputs(Map<String, String> argumentMap)
			throws AzureException {

	}

	/**
	 * Method to make a call to Azure Management API. To get subscription
	 * information we make a GET call to
	 * https://management.core.windows.net/<subscription-id>
	 * 
	 */
	@Override
	protected ClientResponse executeSpecific(Client client)
			throws AzureException {

		ClientResponse response = null;

		WebResource webResource = client.resource(Constants.AZURE_BASE_URL)
				.path(this.subscriptionId);

		LOGGER.info("Calling url " + webResource.getURI());

		response = webResource
				.header(Constants.X_MS_VERSION, this.xmsVersion)
				.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);

		return response;
	}

	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		InputStream inputStream = response.getEntityInputStream();
		
		System.out.println("Subscription details");
		// write formatted xml to System console
		CommonUtil.printFormattedXml(inputStream, System.out, 2);
		
	}

}
