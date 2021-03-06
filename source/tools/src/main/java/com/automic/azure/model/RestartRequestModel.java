package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.automic.azure.constants.Constants;

/**
 * An POJO java class which maps to XML structure which is required to restart the virtual machine.
 */

@XmlRootElement(name = "RestartRoleOperation ", namespace = "http://schemas.microsoft.com/windowsazure")
public class RestartRequestModel {

    private String operationType = Constants.OPERATIONTYPE_RESTART;

    @XmlElement(name = "OperationType", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getOperationType() {
        return operationType;
    }

}