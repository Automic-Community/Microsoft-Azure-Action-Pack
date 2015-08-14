package com.automic.azure.actions;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.utility.CommonUtil;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to fetch Subscription details from Azure for provided Subscription info. Following
 * information is required to fetch subscription details from Azure system:
 * <ul>
 * <li>Subscription Id</li>
 * <li>Authentication Keystore</li>
 * <li>Keystore password</li>
 * </ul>
 *
 */
public class GetSubscriptionInfoAction extends AbstractAction {

	private static final Logger LOGGER = LogManager
			.getLogger(GetSubscriptionInfoAction.class);

	/**
	 * Subscription Id of Azure account
	 */
	private String subscriptionId;

	public GetSubscriptionInfoAction() {
		addOption(Constants.SUBSCRIPTION_ID, true, "Subscription ID");
	}

	@Override
	protected void initialize() {
		subscriptionId = getOptionValue(Constants.SUBSCRIPTION_ID);
	}

	@Override
	protected void validateInputs() throws AzureException {
		if (!Validator.checkNotEmpty(subscriptionId)) {
			LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
			throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
		}

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

		WebResource webResource = client.resource(Constants.AZURE_MGMT_URL)
				.path(this.subscriptionId);

		LOGGER.info("Calling url " + webResource.getURI());

		return webResource.header(Constants.X_MS_VERSION, x_ms_version)
				.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);

	}

	/**
	 * Method to print subscription details xml coming from response as a stream
	 * to standard console. This also prints the xml to Job report in AE.
	 */
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		InputStream inputStream = response.getEntityInputStream();

		CommonUtil.print("Subscription details:\n", LOGGER, StandardLevel.INFO);

		// write formatted xml to System console
		CommonUtil.printFormattedXml(inputStream, System.out, 2);
	}

}