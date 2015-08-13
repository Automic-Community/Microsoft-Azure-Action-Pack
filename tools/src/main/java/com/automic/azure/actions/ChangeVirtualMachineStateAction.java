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
import com.automic.azure.modal.RestartRequestModel;
import com.automic.azure.modal.ShutdownRequestModel;
import com.automic.azure.modal.StartRequestModel;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class will Start, Restart, Shutdown the specified Virtual Machine on Azure Cloud
 * 
 * @author Anurag Upadhyay
 */
public class ChangeVirtualMachineStateAction extends AbstractAction {

  private static final Logger LOGGER = LogManager.getLogger(ChangeVirtualMachineStateAction.class);

  private String subscriptionId;
  private String serviceName;
  private String deploymentName;
  private String vmName;
  private String vmState;

  public ChangeVirtualMachineStateAction() {
    addOption("subscriptionid", true, "Subscription ID");
    addOption("servicename", true, "Azure cloud service name");
    addOption("deploymentname", true, "Azure cloud deployment  name");
    addOption("vmname", true, "Virtual machine name");
    addOption("vmstate", true,
        "Specifies the Virtual Machine operations like start, shutdown, restart");
  }

  @Override
  protected void initialize() {

    serviceName = getOptionValue("servicename");
    deploymentName = getOptionValue("deploymentname");
    vmName = getOptionValue("vmname");
    subscriptionId = getOptionValue("subscriptionid");
    vmState = getOptionValue("vmstate");
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
    if (!Validator.checkNotEmpty(vmName)) {
      LOGGER.error(ExceptionConstants.EMPTY_ROLE_NAME);
      throw new AzureException(ExceptionConstants.EMPTY_ROLE_NAME);
    }
    if (!Validator.checkNotEmpty(vmState)) {
      LOGGER.error(ExceptionConstants.EMPTY_VM_OPERATION_ACTION);
      throw new AzureException(ExceptionConstants.EMPTY_VM_OPERATION_ACTION);
    }
  }

  @Override
  protected ClientResponse executeSpecific(Client client) throws AzureException {
    ClientResponse response = null;
    WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(subscriptionId)
        .path("services").path("hostedservices").path(serviceName).path("deployments")
        .path(deploymentName).path("roleinstances").path(vmName).path("Operations");
    LOGGER.info("Calling url " + webResource.getURI());
    response = webResource.entity(getRequestBody(vmState), MediaType.APPLICATION_XML)
        .header(Constants.X_MS_VERSION, x_ms_version).post(ClientResponse.class);
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
      throw new AzureException(
          "No rquested operation found for [Start, Stopped, StoppedDeallocated, Restart] virtual machine");
    }
    return obj;
  }

  /**
   * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} it will print request token id.
   * 
   */
  @Override
  protected void prepareOutput(ClientResponse response) throws AzureException {
    List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
    print("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0), LOGGER, StandardLevel.INFO);

  }
}
