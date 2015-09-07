package com.automic.azure.constants;



/**
 * Enum that defines constants which are placeholder for actions. When an implementation of {@link AbstractAction} is
 * created we also create a constant in this enum. Mapping is defined in {@link ActionFactory}
 */
public enum Action {

    VERSION, GET_SUBSCRIPTION_INFO, VM_STATE, CHECK_REQUEST_STATUS, DELETE_VM, 
    CREATE_VM_DEPLOYMENT, CREATE_STORAGE_CONTAINER, PUT_BLOB, DELETE_STORAGE_CONTAINER;

    public static String getActionNames() {
        Action[] actions = Action.values();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < actions.length; i++) {
            sb.append(actions[i].name());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

}
