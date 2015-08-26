package com.automic.azure.actions;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to fetch Subscription details from Azure for provided Subscription info. Following information is
 * required to fetch subscription details from Azure system:
 * <ul>
 * <li>Subscription Id</li>
 * <li>Authentication Keystore</li>
 * <li>Keystore password</li>
 * </ul>
 *
 */
public final class GetSubscriptionInfoAction extends AbstractManagementAction {

    private static final Logger LOGGER = LogManager.getLogger(GetSubscriptionInfoAction.class);

    
    public GetSubscriptionInfoAction() {
        super();
    }

    @Override
    protected void initializeActionSpecificArgs() {
       
    }

    @Override
    protected void validateActionSpecificInputs() throws AzureException {
        
    }

    /**
     * Method to make a call to Azure Management API. To get subscription information we make a GET call to
     * https://management.core.windows.net/<subscription-id>
     * 
     */
    @Override
    protected ClientResponse executeSpecific(Client client) throws AzureException {

        WebResource webResource = client.resource(Constants.AZURE_MGMT_URL).path(this.subscriptionId);

        LOGGER.info("Calling url " + webResource.getURI());

        return webResource.header(Constants.X_MS_VERSION, restapiVersion).accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

    }

    /**
     * Method to print subscription details xml coming from response as a stream to standard console. This also prints
     * the xml to Job report in AE.
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws AzureException {
        InputStream inputStream = response.getEntityInputStream();

        ConsoleWriter.writeln("Subscription details:");
        // write formatted xml to System console
        CommonUtil.printFormattedXml(inputStream, ConsoleWriter.getStream(), 2);
    }

}