/**
 * 
 */
package com.automic.azure.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.automic.azure.constants.Constants;

/**
 * @author sumitsamson
 *
 */

/**
 * This class is used to instantiate the Options for azure.This class only initializes the action and help options.Further action specific 
 * options should be added to the same object (i.e azureOptions object) instantiated by this class*/
public final class AzureOptions {
	
	private static final boolean ISREQUIRED = true;
	private static Options azureOpts = new Options();
	
	public static Options getAzureOptions() {
		return azureOpts;
	}	
	
	public static Options initializeActionOptions(){
		
		azureOpts.addOption(Option.builder(Constants.ACTION).required(ISREQUIRED).hasArg().desc("Name of the action").build());		
		azureOpts.addOption(Option.builder(Constants.HELP).required(!ISREQUIRED).desc("show help.").build());	
		
		return azureOpts;
	}
	
	

}
