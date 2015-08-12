/**
 * 
 */
package com.automic.azure.actions;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.utility.CommonUtil;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @author sumitsamson
 * 
 */
public class GetSubscriptionInfoAction extends AbstractAction {

	private static final Logger LOGGER = LogManager.getLogger(GetSubscriptionInfoAction.class);

	private String filePath;
	private final String FILE_LONG_OPT = "outputFile";
	private final String FILE_DESC = "Output file location";
	private String subscriptionId;

	

	@Override
	protected void initialize() {
		filePath = cmd.getOptionValue(Constants.OUTPUT_FILE);
		subscriptionId = cmd.getOptionValue(Constants.SUBSCRIPTION_ID);
	}

	@Override
	protected void validateInputs() throws AzureException {
		if (!Validator.checkNotEmpty(subscriptionId)) {
			LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
			throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
		}
		if (!Validator.checkFileFolderExists(filePath)) {
			LOGGER.error(ExceptionConstants.INVALID_FILE);
			throw new AzureException(String.format(ExceptionConstants.INVALID_FILE, filePath));
		}
	}

	@Override
	protected ClientResponse executeSpecific(Client client) throws AzureException {

		ClientResponse response = null;

		WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(this.subscriptionId);

		LOGGER.info("Calling url " + webResource.getURI());

		response = webResource.header(Constants.X_MS_VERSION, x_ms_version)
				.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);

		return response;
	}

	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		CommonUtil.createFile(filePath, response.getEntity(String.class));
	}

	@Override
	protected void addOptions() {
		addOption(Constants.SUBSCRIPTION_ID, true, "Subscription ID", true);
		addOption(FILE_LONG_OPT, true, FILE_DESC, true);
		
	}

}
