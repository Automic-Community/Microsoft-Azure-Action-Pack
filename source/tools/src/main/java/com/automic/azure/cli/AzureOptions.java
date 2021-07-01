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
 * This class is used to instantiate the Options for azure.This class only initializes the action and help
 * options.Further action specific options should be added to the same object (i.e azureOptions object) instantiated by
 * this class
 */
public final class AzureOptions {

    private static final boolean ISREQUIRED = true;
    private final Options azureOpts;

    public AzureOptions() {
        azureOpts = new Options();
        azureOpts.addOption(Option.builder(Constants.ACTION).required(ISREQUIRED).hasArg().desc("Action Name").build());
        azureOpts.addOption(Option.builder(Constants.HELP).required(!ISREQUIRED).desc("Usage").build());
    }

    public void addOption(String optionName, boolean isRequired, String description) {
        azureOpts.addOption(Option.builder(optionName).required(isRequired).hasArg().desc(description).build());
    }
    
    Options getOptions() {
        return azureOpts;
    }
    
}
