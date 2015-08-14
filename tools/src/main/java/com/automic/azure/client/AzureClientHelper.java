/**
 * 
 */
package com.automic.azure.client;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.actions.AbstractAction;
import com.automic.azure.actions.ActionFactory;
import com.automic.azure.constants.Action;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;


/**
 * Helper class to delegate request to specific Action based on input arguments .
 * */
public final class AzureClientHelper {

    private static final Logger LOGGER = LogManager.getLogger(AzureClientHelper.class);
    
    private AzureClientHelper() {
    }

    /**
     * Method to delegate parameters to an instance of {@link AbstractAction} based on the value of Action parameter. 
     * @param args array of String args
     * @throws AzureException 
     */
    public static void executeAction(String[] args) throws AzureException {
        String action = args[0].trim();
        if (action.isEmpty()) {
            LOGGER.error(ExceptionConstants.INVALID_ACTION);
            throw new AzureException(ExceptionConstants.INVALID_ACTION);
        }
        action = action.toUpperCase();
        LOGGER.info("Execution starts for action [" + action + "]...");
        AbstractAction useraction = ActionFactory.getAction(Action.valueOf(action));
        useraction.executeAction(Arrays.copyOfRange(args, 1, args.length));
    }
}
