package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.automic.azure.util.Validator;

/**
 * An POJO java class which maps to XML structure which is required to handle the error response.
 */

@XmlRootElement(name = "Error")
public class AzureStorageErrorResponse  implements ErrorResponse {

    private String code;
    private String message;
    private String authenticationErrorDetail;

    @XmlElement(name = "Code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlElement(name = "Message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement(name = "AuthenticationErrorDetail")
    public String getAuthenticationErrorDetail() {
        return authenticationErrorDetail;
    }

    public void setAuthenticationErrorDetail(String authenticationErrorDetail) {
        this.authenticationErrorDetail = authenticationErrorDetail;
    }

    @Override
    public String toString() {
        StringBuilder responseBuilder = new StringBuilder("Azure Response: ");
        responseBuilder.append("Error Code: [");
        responseBuilder.append(this.code).append("]");
        if (Validator.checkNotEmpty(this.message)) {
            responseBuilder.append(" Message: ").append(this.message);
        }
        if (Validator.checkNotEmpty(this.authenticationErrorDetail)) {
            responseBuilder.append(" AuthenticationErrorDetail: ").append(this.authenticationErrorDetail);
        }
        return responseBuilder.toString();
    }

}
