/**
 * 
 */
package com.automic.azure.actions;

import com.automic.azure.exception.AzureException;

/**
 * Interface of all actions in this Azure Client Package
 *
 */
public interface IAzureAction {

	/**
	 * 
	 * @param commandLineArgs
	 * @throws AzureException
	 */
	public void executeAction(String[] commandLineArgs) throws AzureException ;
	
	
}
