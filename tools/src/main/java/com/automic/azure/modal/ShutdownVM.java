package com.automic.azure.modal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ShutdownRoleOperation ", namespace="http://schemas.microsoft.com/windowsazure") 
public class ShutdownVM {
	
	private String operationType;
	private String postShutdownAction;
	
	@XmlElement(name="OperationType", namespace="http://schemas.microsoft.com/windowsazure") 
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	@XmlElement(name="PostShutdownAction", namespace="http://schemas.microsoft.com/windowsazure") 
	public String getPostShutdownAction() {
		return postShutdownAction;
	}
	public void setPostShutdownAction(String postShutdownAction) {
		this.postShutdownAction = postShutdownAction;
	}
	
}
