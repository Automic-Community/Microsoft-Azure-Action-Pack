package com.automic.azure.modal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.automic.azure.constants.Constants;


@XmlRootElement(name="RestartRoleOperation ", namespace="http://schemas.microsoft.com/windowsazure") 
public class RestartResquestModel {
	
	private String operationType = Constants.OPERATIONTYPE_RESTART;
	
	@XmlElement(name="OperationType", namespace="http://schemas.microsoft.com/windowsazure") 
	public String getOperationType() {
		return operationType;
	}	
	

}