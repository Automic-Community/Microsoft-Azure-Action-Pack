package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.automic.azure.constants.Constants;

/**
 * An POJO java class which maps to XML structure which is required to shutdown the virtual machine.
 */

@XmlRootElement(name = "ShutdownRoleOperation ", namespace = "http://schemas.microsoft.com/windowsazure")
public class ShutdownRequestModel {

    private String operationType = Constants.OPERATIONTYPE_SHUTDOWN;
    private String postShutdownAction;

    @SuppressWarnings("unused")
    private ShutdownRequestModel() {
    }

    public ShutdownRequestModel(String postShutdownAction) {
        this.postShutdownAction = postShutdownAction;
    }

    @XmlElement(name = "OperationType", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getOperationType() {
        return operationType;
    }

    @XmlElement(name = "PostShutdownAction", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getPostShutdownAction() {
        return postShutdownAction;
    }
}
