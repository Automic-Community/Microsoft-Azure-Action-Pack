package com.automic.azure.modal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.automic.azure.constants.Constants;

@XmlRootElement(name="ShutdownRoleOperation ", namespace="http://schemas.microsoft.com/windowsazure") 
public class ShutdownVM {
	
	private String operationType = Constants.OPERATIONTYPE_SHUTDOWN;
	private String postShutdownAction;
	
	@XmlElement(name="OperationType", namespace="http://schemas.microsoft.com/windowsazure") 
	public String getOperationType() {
		return operationType;
	}	
	
	@XmlElement(name="PostShutdownAction", namespace="http://schemas.microsoft.com/windowsazure") 
	public String getPostShutdownAction() {
		return postShutdownAction;
	}
	public void setPostShutdownAction(String postShutdownAction) {
		this.postShutdownAction = postShutdownAction;
	}
	
}
