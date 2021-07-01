/**
 * 
 */
package com.automic.azure.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.actions.AbstractAction;
import com.automic.azure.actions.ActionFactory;
import com.automic.azure.cli.AzureCli;
import com.automic.azure.cli.AzureOptions;
import com.automic.azure.constants.Action;
import com.automic.azure.constants.Constants;
import com.automic.azure.exception.AzureException;

/**
 * Helper class to delegate request to specific Action based on input arguments .
 * */
public final class AzureClientHelper {
    
    private static final Logger LOGGER = LogManager.getLogger(AzureClientHelper.class);
    
    private AzureClientHelper() {
    }    

    /**
     * Method to delegate parameters to an instance of {@link AbstractAction} based on the value of Action parameter.
     * 
     * @param map
     *            of options with key as option name and value is option value
     * @throws AzureException
     */
    public static void executeAction(String[] args) throws AzureException {
        String action = new AzureCli(new AzureOptions(), args).getOptionValue(Constants.ACTION).toUpperCase();
        LOGGER.info("Execution starts for action [" + action + "]...");
        AbstractAction useraction = ActionFactory.getAction(Action.valueOf(action));
        useraction.executeAction(args);
    }
}
