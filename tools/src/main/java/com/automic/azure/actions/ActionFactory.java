package com.automic.azure.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Action;
import com.automic.azure.exceptions.AzureException;

/**
 * Factory class to create instances of implementations of
 * {@link AbstractAction}. This class will create the instances based on
 * {@link Action} parameter. It throws an Exception if no matching
 * implementation could be found.
 */
public final class ActionFactory {

	private static final Logger LOGGER = LogManager
			.getLogger(AbstractAction.class);

	private ActionFactory() {
	}

	/**
	 * Method to return instance of implementation of {@link AbstractAction}
	 * based on value of enum {@link Action} passed.
	 * 
	 * @param enumAction
	 * @return an implementation of {@link AbstractAction}
	 * @throws AzureException
	 *             if no matching implementation could be found
	 */
	public static AbstractAction getAction(Action enumAction)
			throws AzureException {

		AbstractAction action = null;

		switch (enumAction) {
		case VERSION:
			action = new GetSubscriptionInfoAction();
			break;
		case START_ROLE:
			action = new StartRoleAction();
			break;
		case RESTART_ROLE:
			action = new RestartRoleAction();
			break;
		case SHUTDOWN_ROLE:
			action = new ShutdownRoleAction();
			break;
		default:
			String msg = "Invalid Action.. Please enter valid action "
					+ Action.getActionNames();
			LOGGER.error(msg);
			throw new AzureException(msg);
		}
		return action;
	}

}
