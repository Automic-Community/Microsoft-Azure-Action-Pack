package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.automic.azure.constants.Constants;

/**
 * An POJO java class which maps to XML structure which is required to handle the operation response.
 */

@XmlRootElement(name = "Operation", namespace = Constants.AZURE_ERROR_NAMESPACE)
public final class AzureRequestStatusModel {

    private String requestTokenId;
    private String requestStatus;
    private String httpStatusCode;
    private AzureErrorResponse error;

    @XmlElement(name = "ID", namespace = Constants.AZURE_ERROR_NAMESPACE)
    public String getRequestTokenId() {
        return requestTokenId;
    }

    public void setRequestTokenId(String requestTokenId) {
        this.requestTokenId = requestTokenId;
    }

    @XmlElement(name = "Status", namespace = Constants.AZURE_ERROR_NAMESPACE)
    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    @XmlElement(name = "HttpStatusCode", namespace = Constants.AZURE_ERROR_NAMESPACE)
    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    @XmlElement(name = "Error", namespace = Constants.AZURE_ERROR_NAMESPACE)
    public AzureErrorResponse getError() {
        return error;
    }

    public void setError(AzureErrorResponse error) {
        this.error = error;
    }

}
