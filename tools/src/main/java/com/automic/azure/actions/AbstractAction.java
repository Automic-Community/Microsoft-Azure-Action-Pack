/**
 * 
 */
package com.automic.azure.actions;

import java.util.List;

import com.automic.azure.cli.AzureCli;
import com.automic.azure.cli.AzureOptions;
import com.automic.azure.exception.AzureBusinessException;

/**
 * An abstract action which parses the command line parameters using apache cli and further calls the execute method.
 * The implementation of execute method will be provided by the subclass of this class. This class also provides the
 * method to retrieve the arguments which can be used inside execute method.
 */
public abstract class AbstractAction {

    private AzureOptions actionOptions;
    private AzureCli cli;

    public AbstractAction() {
        actionOptions = new AzureOptions();
    }

    /**
     * This method is used to add the argument.
     * 
     * @param optionName
     *            argument key used to identify the argument.
     * @param isRequired
     *            true/false. True means argument is mandatory otherwise it is optional.
     * @param description
     *            represents argument description.
     */
    public final void addOption(String optionName, boolean isRequired, String description) {
        actionOptions.addOption(optionName, isRequired, description);
    }

    /**
     * This method is used to retrieve the value of specified argument.
     * 
     * @param arg
     *            argument key for which you want to get the value.
     * @return argument value for the specified argument key.
     */
    public final String getOptionValue(String arg) {
        return cli.getOptionValue(arg);
    }

    /**
     * This method initializes the arguments and calls the execute method.
     * 
     * @throws AzureBusinessException
     *             exception while executing an action
     */
    public final void executeAction(String[] commandLineArgs) throws AzureBusinessException {
        cli = new AzureCli(actionOptions, commandLineArgs);
        cli.log(noLogging());
        execute();
    }

    /**
     * Method to retrieve the argument keys that we don't need to log.
     */
    protected abstract List<String> noLogging();

    /**
     * Method to execute the action.
     * 
     * @throws AzureBusinessException
     */
    protected abstract void execute() throws AzureBusinessException;

}
