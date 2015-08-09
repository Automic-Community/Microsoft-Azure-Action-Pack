/**
 * 
 */
package com.automic.azure.config;

import java.util.Map;

import org.apache.commons.cli.Options;

import com.automic.azure.exceptions.AzureException;

/**
 * @author sumitsamson
 *
 */
public interface IAzureOptions {	
	
	/**
	 * This function initializes the options for a given action.
	 **/
	public  Options initializeOptions();
	
	/**
	 * This function parses the array of args with the given Options initialized by the above function initializeOptions()
	 * for an action and returns a Map<String,String> which contains option short form as key and option value as value.
	 * for e.g --act VERSION ,then key=act & value=VERSION
	 **/
	public  Map<String, String> parseCommandLine(String [] args) throws AzureException;
	
	/**
	 * This function prints the help for a given action
	 **/
	public  void help(String action,Options options);

}
