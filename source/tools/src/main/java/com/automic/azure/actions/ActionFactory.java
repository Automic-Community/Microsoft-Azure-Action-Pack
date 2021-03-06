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
            case DELETE_VM:
                action = new DeleteVMaction();
                break;
            case CREATE_VM_DEPLOYMENT:
                action = new CreateVirtualMachineDeploymentAction();
                break;
            case CREATE_STORAGE_CONTAINER:
                action = new CreateStorageContainerAction();
                break;
            case DELETE_STORAGE_CONTAINER:
                action = new DeleteStorageContainerAction();
                break;
            case CREATE_CLOUD_SERVICE:
                action = new CreateCloudServiceAction();
                break;
            case PUT_BLOB:
                action = new PutBlockBlobAction();
                break;
            case DELETE_BLOB:
                action = new DeleteBlobAction();
                break;
            case DELETE_FILE:
                action = new DeleteFileAction();
                break;
            case SET_VIRTUAL_NETWORK:
                action = new VirtualNetworkConfigurationAction();
                break;
            case DELETE_CLOUD_SERVICE:
                action = new DeleteCloudServiceAction();
                break;
            case DELETE_DEPLOYMENT:
                action = new DeleteDeploymentAction();
                break;
            case CREATE_DEPLOYMENT:
                action = new CreateDeploymentAction();
                break;
            default:
                String msg = "Invalid Action.. Please enter valid action " + Action.getActionNames();
                LOGGER.error(msg);
                throw new AzureException(msg);
        }
        return action;
    }

}
