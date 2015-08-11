/**
 * 
 */
package com.automic.azure.actions;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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

    private static final int NO_OF_ARGS = 6;
    
    private String filePath;
    private final String FILE_LONG_OPT = "outputFile";
    private final String FILE_DESC = "Output file location";
	

	
//	public GetSubscriptionInfoAction() {
//		super(NO_OF_ARGS);
//	}
	
	
	@Override
	protected Options initializeOptions() {	
		 actionOptions.addOption(Option.builder(Constants.OUTPUT_FILE).required(true).hasArg().longOpt(FILE_LONG_OPT).desc(FILE_DESC).build());
		return actionOptions;
	}
	
	@Override
	protected void logParameters(Map<String, String> argumentMap) {
		LOGGER.info(argumentMap);

	}
	
	@Override
	protected void initialize(Map<String, String> argumentMap) {
		filePath = argumentMap.get(Constants.OUTPUT_FILE);	
	}

	@Override
	protected void validateInputs(Map<String, String> argumentMap) throws AzureException {		
		if (!Validator.checkFileFolderExists(filePath)) {
			LOGGER.error(ExceptionConstants.INVALID_FILE);
			throw new AzureException(String.format(ExceptionConstants.INVALID_FILE, filePath));
		}
	}

	@Override
	protected ClientResponse executeSpecific(Client client) throws AzureException {
		
		ClientResponse response = null;

        WebResource webResource = client.resource(Constants.AZURE_BASE_URL).path(this.subscriptionId);
        

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.header(Constants.X_MS_VERSION,Constants.X_MS_VERSION_VALUE).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        return response;
	}
	
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		CommonUtil.createFile(filePath, response.getEntity(String.class));
	}

	
}
