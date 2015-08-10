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
public final class AzureOptions {
	
	private static Options compulsoryOptions = new Options();
	
	private static final boolean ISREQUIRED = true;
	
	public static Options initializeCompulsoryOptions(){
		
		compulsoryOptions.addOption(Option.builder(Constants.ACTION).required(ISREQUIRED).hasArg().longOpt("action").desc("Name of the action").build());
		compulsoryOptions.addOption(Option.builder(Constants.READ_TIMEOUT).required(ISREQUIRED).hasArg().longOpt("readtimeout").desc("Read timeout").build());
		compulsoryOptions.addOption(Option.builder(Constants.CONNECTION_TIMEOUT).required(ISREQUIRED).hasArg().longOpt("connectiontimeout").desc("connection timeout").build());
		compulsoryOptions.addOption(Option.builder(Constants.SUBSCRIPTION_ID).required(ISREQUIRED).hasArg().longOpt("subscriptionId").desc("Subscription ID").build());
		compulsoryOptions.addOption(Option.builder(Constants.KEYSTORE_LOCATION).required(ISREQUIRED).hasArg().longOpt("keystore").desc("Keystore location").build());
		compulsoryOptions.addOption(Option.builder(Constants.PASSWORD).required(ISREQUIRED).hasArg().longOpt("password").desc("Keystore password").build());
		compulsoryOptions.addOption(Option.builder(Constants.HELP).required(!ISREQUIRED).longOpt("help").desc("show help.").build());	
		
		return compulsoryOptions;
	}

}
