package com.automic.azure.actions;

/**
 * 
 */

import static com.automic.azure.utility.CommonUtil.print;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.modal.StartRequestModel;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class will starts the specified Virtual Machine on Azure Cloud
 */
public class StartVMAction extends AbstractAction {

	private static final Logger LOGGER = LogManager
			.getLogger(StartVMAction.class);

	private static final String SERVICE_OPT = "servicename";
	private static final String SERVICE_DESC = "Azure cloud service name";
	private static final String DEPLOYMENT_OPT = "deploymentname";
	private static final String DEPLOYMENT_DESC = "Azure cloud deployment  name";
	private static final String ROLE_OPT = "rolename";
	private static final String ROLE_DESC = "Role name (VM name)";
	private String subscriptionId;
	private String serviceName;
	private String deploymentName;
	private String roleName;

	
	@Override

	protected void addOptions() {
		addOption(Constants.SUBSCRIPTION_ID, true, "Subscription ID", true);
		addOption(SERVICE_OPT, true, SERVICE_DESC, true);
		addOption(DEPLOYMENT_OPT, true, DEPLOYMENT_DESC, true);
		addOption(ROLE_OPT, true, ROLE_DESC, true);
	}

	@Override
	protected void initialize() {

		serviceName = getOptions().getOptionValue(SERVICE_OPT);
		deploymentName = getOptions().getOptionValue(DEPLOYMENT_OPT);
		roleName = getOptions().getOptionValue(ROLE_OPT);
		subscriptionId = getOptions().getOptionValue(Constants.SUBSCRIPTION_ID);
	}

	@Override
	protected void validateInputs() throws AzureException {
		if (!Validator.checkNotEmpty(subscriptionId)) {
			LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
			throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
		}
		if (!Validator.checkNotEmpty(serviceName)) {
			LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
		}
		if (!Validator.checkNotEmpty(deploymentName)) {
			LOGGER.error(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
		}
		if (!Validator.checkNotEmpty(roleName)) {
			LOGGER.error(ExceptionConstants.EMPTY_ROLE_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_ROLE_NAME);
		}
	}

	@Override
	protected ClientResponse executeSpecific(Client client)
			throws AzureException {
		ClientResponse response = null;


		WebResource webResource = client.resource(Constants.AZURE_MGMT_URL)
				.path(subscriptionId).path(Constants.SERVICES_PATH)
				.path(Constants.HOSTEDSERVICES_PATH).path(serviceName)
				.path(Constants.DEPLOYMENTS_PATH).path(deploymentName)
				.path(Constants.ROLEINSTANCES_PATH).path(roleName)
				.path(Constants.OPERATIONS_PATH);
		LOGGER.info("Calling url " + webResource.getURI());

		response = webResource.entity(new StartRequestModel(), MediaType.APPLICATION_XML)
		.header(Constants.X_MS_VERSION, x_ms_version)
				.post(ClientResponse.class);

		return response;
	}

	/**
	 * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} it will print
	 * request token id.
	 * 
	 */
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		List<String> tokenid = response.getHeaders().get(
				Constants.REQUEST_TOKENID_KEY);
		print("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0), LOGGER,
				StandardLevel.INFO);
	}
}
