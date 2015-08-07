/**
 * 
 */
package com.automic.azure.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.exceptions.AzureException;

/**
 * @author sumitsamson
 * 
 */
public final class AzureOptions {

	private static final Logger LOGGER = LogManager.getLogger(AzureOptions.class);

	private Options options = new Options();
	private Map<String, String> argsMap = new HashMap<String, String>(10);

	private String[] args = null;

	public AzureOptions(String args[]) {
		this.args = args;

		options.addOption("act", "action", true, "Name of the action");
		options.addOption("rto", "readtimeout", true, "Read timeout");
		options.addOption("cto", "connectiontimeout", true, "connection timeout");
		options.addOption("sid", "subscriptionId", true, "Subscription ID");
		options.addOption("ksl", "keystore", true, "Keystore location");
		options.addOption("pwd", "password", true, "Keystore password");
		options.addOption("url", "azureurl", true, "Azure cloud url");
		options.addOption("ser", "servicename", true, "Azure cloud service name");
		options.addOption("dep", "deploymentname", true, "Azure deployment  name");
		options.addOption("rol", "rolename", true, "Role name (VM name)");
		options.addOption("h",   "help", false, "show help.");

	}

	public Map<String, String> parse() throws AzureException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			LOGGER.error("Error parsing the command line options ", e);
			help();
			throw new AzureException("Invalid args ", e);
		}

		if (cmd.hasOption('h') || cmd.hasOption("help")) {
			help();
		} else {
			for (Option option : cmd.getOptions()) {
				argsMap.put(option.getOpt(), option.getValue().trim());
			}
		}
		return argsMap;

	}

	private void help() {
		HelpFormatter formater = new HelpFormatter();

		formater.printHelp("Azure", options);

	}

}
