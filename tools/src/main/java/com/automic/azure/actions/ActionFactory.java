package com.automic.azure.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Action;
import com.automic.azure.exception.AzureException;

/**
 * Factory class to create instances of implementations of {@link AbstractAction}. This class will create the instances
 * based on {@link Action} parameter. It throws an Exception if no matching implementation could be found.
 */
public final class ActionFactory {

    private static final Logger LOGGER = LogManager.getLogger(AbstractAction.class);

    private ActionFactory() {
    }

    /**
     * Method to return instance of implementation of {@link AbstractAction} based on value of enum {@link Action}
     * passed.
     * 
     * @param enumAction
     * @return an implementation of {@link AbstractAction}
     * @throws AzureException
     *             if no matching implementation could be found
     */
    public static AbstractAction getAction(Action enumAction) throws AzureException {

        AbstractAction action = null;

        switch (enumAction) {
            case VM_STATE:
                action = new ChangeVirtualMachineStateAction();
                break;
            case GET_SUBSCRIPTION_INFO:
                action = new GetSubscriptionInfoAction();
                break;
            case CHECK_REQUEST_STATUS:
                action = new CheckRequestStatusAction();
                break;
            default:
                String msg = "Invalid Action.. Please enter valid action " + Action.getActionNames();
                LOGGER.error(msg);
                throw new AzureException(msg);
        }
        return action;
    }

}
