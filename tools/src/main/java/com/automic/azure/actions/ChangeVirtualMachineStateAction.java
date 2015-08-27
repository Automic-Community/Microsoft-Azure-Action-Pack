package com.automic.azure.actions;

/**
 * 
 */

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.RestartRequestModel;
import com.automic.azure.model.ShutdownRequestModel;
import com.automic.azure.model.StartRequestModel;
import com.automic.azure.util.ConsoleWriter;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class will Start, Restart, Shutdown the specified Virtual Machine on
 * Azure Cloud
 * 
 * 
 */
public final class ChangeVirtualMachineStateAction extends AbstractManagementAction {

	private static final Logger LOGGER = LogManager.getLogger(ChangeVirtualMachineStateAction.class);

	private String serviceName;
	private String deploymentName;
	private String vmName;
	private String vmState;

	public ChangeVirtualMachineStateAction() {
		addOption("servicename", true, "Azure cloud service name");
		addOption("deploymentname", true, "Azure cloud deployment name");
		addOption("vmname", true, "Virtual machine name");
		addOption("vmstate", true, "Virtual Machine Command(Start|Stopped|StoppedDeallocated|Restart)");
	}

	@Override
	protected void initializeActionSpecificArgs() {

		serviceName = getOptionValue("servicename");
		deploymentName = getOptionValue("deploymentname");
		vmName = getOptionValue("vmname");
		vmState = getOptionValue("vmstate");
	}

	@Override
	protected void validateActionSpecificInputs() throws AzureException {
		if (!Validator.checkNotEmpty(this.serviceName)) {
			LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
		}
		if (!Validator.checkNotEmpty(this.deploymentName)) {
			LOGGER.error(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
		}
		if (!Validator.checkNotEmpty(this.vmName)) {
			LOGGER.error(ExceptionConstants.EMPTY_ROLE_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_ROLE_NAME);
		}
		if (!Validator.checkNotEmpty(vmState)) {
			LOGGER.error(ExceptionConstants.EMPTY_VM_OPERATION_ACTION);
			throw new AzureException(ExceptionConstants.EMPTY_VM_OPERATION_ACTION);
		}
	}

	/**
	 * Method to make a call to Azure Management API. To get subscription
	 * information we make a GET call to
	 * https://management.core.windows.net/<subscription-id>
	 * 
	 */
	@Override
	protected ClientResponse executeSpecific(Client client) throws AzureException {
		ClientResponse response = null;
		WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId)
				.path("services")
				.path("hostedservices").path(serviceName).path("deployments").path(deploymentName)
				.path("roleinstances").path(vmName).path("Operations");
		LOGGER.info("Calling url " + webResource.getURI());
		response = webResource.entity(getRequestBody(vmState), MediaType.APPLICATION_XML)
				.header(Constants.X_MS_VERSION, restapiVersion).post(ClientResponse.class);
		return response;
	}

	/**
	 * This method return the request body object
	 * 
	 * @param operation
	 * @return Object
	 */

	private Object getRequestBody(String vmState) throws AzureException {
		Object obj = null;
		switch (vmState.toUpperCase()) {
		case "START":
			obj = new StartRequestModel();
			break;
		case "STOPPEDDEALLOCATED":
		case "STOPPED":
			obj = new ShutdownRequestModel(vmState);
			break;
		case "RESTART":
			obj = new RestartRequestModel();
			break;
		default:
			throw new AzureException(String.format(ExceptionConstants.INVALID_VMSTATE_COMMAND, vmState,
					"Start|Stopped|StoppedDeallocated|Restart"));
		}
		return obj;
	}

	/**
	 * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} it will print
	 * request token id.
	 * 
	 */
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
		ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));
	}

}
