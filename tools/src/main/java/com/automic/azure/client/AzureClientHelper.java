/**
 * 
 */
package com.automic.azure.client;

import java.util.Map;

import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.actions.AbstractAction;
import com.automic.azure.actions.ActionFactory;
import com.automic.azure.cli.AzureOptions;
import com.automic.azure.constants.Action;
import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.utility.CommonUtil;


/**
 * Helper class to delegate request to specific Action based on input arguments .
 * */
public final class AzureClientHelper {

    private static final Logger LOGGER = LogManager.getLogger(AzureClientHelper.class);
    
    
    /**
     * Method to delegate parameters to an instance of {@link AbstractAction} based on the value of Action parameter. 
     * @param map of options with key as option name and value is option value
     * @throws AzureException 
     */    
    public static void executeAction(String [] orignalArgs) throws AzureException{    	
    	 Options compulsoryOptions = AzureOptions.initializeCompulsoryOptions();
    	
    	 Map<String, String> argsMap = CommonUtil.parseCommandLine(compulsoryOptions,orignalArgs);
    	 
    	 if (argsMap.size() > 0 && argsMap.containsKey(Constants.ACTION)){
    		 String action = argsMap.get(Constants.ACTION).toUpperCase();
    		 LOGGER.info("Execution starts for action [" + action + "]...");
 	         AbstractAction useraction = ActionFactory.getAction(Action.valueOf(action)); 
 	        useraction.executeAction(compulsoryOptions,orignalArgs,action);
    	 }else {
    		 LOGGER.error(ExceptionConstants.INVALID_ACTION);
	            throw new AzureException(ExceptionConstants.INVALID_ACTION);
    	 }    	
    	
    }
    
    private AzureClientHelper() {
    }
    
}
