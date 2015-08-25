package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An POJO java class which maps to XML structure which is required to handle the error response.
 */

@XmlRootElement(name = "Error", namespace = "http://schemas.microsoft.com/windowsazure")
public class AzureErrorResponse {

    private String code;
    private String message;

    @XmlElement(name = "Code", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlElement(name = "Message", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Azure Error Response ");
		if (code != null) {
			builder.append("[code=");
			builder.append(code);
			builder.append("], ");
		}
		if (message != null) {
			builder.append("[message=");
			builder.append(message);
		}
		builder.append("]");
		return builder.toString();
	}

	
	

}
