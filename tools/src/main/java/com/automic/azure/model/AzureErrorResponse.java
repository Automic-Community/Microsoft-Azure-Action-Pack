package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.automic.azure.util.Validator;

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
        StringBuilder responseBuilder = new StringBuilder("Azure Response: ");
        responseBuilder.append("Error Code: [");
        responseBuilder.append(this.code).append("]");
        if (Validator.checkNotEmpty(this.message)) {
            responseBuilder.append(" Message: ").append(this.message);
        }
        return responseBuilder.toString();
    }

}
