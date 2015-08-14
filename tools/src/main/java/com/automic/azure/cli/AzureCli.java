package com.automic.azure.cli;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;

/**
 * This class is used to parse the arguments against provided options using apache cli library.
 * Further this class provides method to retrieve the argument value.
 */
public class AzureCli {
    
    private static final Logger LOGGER = LogManager.getLogger(AzureCli.class);
    
    private CommandLine cmd = null;
    
    public AzureCli(AzureOptions options, String[] args) throws AzureException {        
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options.getOptions(), args, true);
        } catch (ParseException e) {
            LOGGER.error("Error parsing the command line options", e);
            printHelp(options.getOptions());
            throw new AzureException(String.format(ExceptionConstants.INVALID_ARGS, e.getMessage()));
        }
    }
    
    public String getOptionValue(String arg) {
        String value = cmd.getOptionValue(arg);
        return (value != null ? value.trim() : value);
    }

    public void log(List<String> ignoreOptions) {
        LOGGER.info("Input params ");
        for (Option o : cmd.getOptions()) {
            if (!ignoreOptions.contains(o.getOpt())) {
                LOGGER.info(o.getDescription() + "[" + o.getOpt() + "]" + " = " + o.getValue());
            }
        }
    }
    
    private void printHelp(Options options) {
        HelpFormatter formater = new HelpFormatter();        
        formater.printHelp("Usage ", options);
    }

}
