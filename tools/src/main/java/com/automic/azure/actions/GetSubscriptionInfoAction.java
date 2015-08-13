/**
 * 
 */
package com.automic.azure.actions;

import java.io.InputStream;

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
 * Action class to fetch Subscription details from Azure for provided Subscription info. Following
 * information is required to fetch subscription details from Azure system:
 * <ul>
 * <li>Subscription Id</li>
 * <li>Authentication Certificate</li>
 * <li>Certificate password</li>
 * </ul>
 *
 */
public class GetSubscriptionInfoAction extends AbstractAction {

  private static final Logger LOGGER = LogManager.getLogger(GetSubscriptionInfoAction.class);

  private String subscriptionId;
  
  public GetSubscriptionInfoAction() {
    addOption("subscriptionId", true, "Subscription ID");
  } 

  @Override
  protected void initialize() {
    subscriptionId = getOptionValue("subscriptionId");
  }

  @Override
  protected void validateInputs() throws AzureException {
    if (!Validator.checkNotEmpty(subscriptionId)) {
      LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
      throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
    }

  }

  /**
   * Method to make a call to Azure Management API. To get subscription information we make a GET
   * call to https://management.core.windows.net/<subscription-id>
   * 
   */
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
    InputStream inputStream = response.getEntityInputStream();

    System.out.println("Subscription details");
    // write formatted xml to System console
    CommonUtil.printFormattedXml(inputStream, System.out, 2);

  }

}