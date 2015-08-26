package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An POJO java class which maps to XML structure which is required to handle the operation response.
 */

@XmlRootElement(name = "Operation", namespace = "http://schemas.microsoft.com/windowsazure")
public final class AzureRequestStatusModel {

    private String requestTokenId;
    private String requestStatus;
    private String httpStatusCode;
    private AzureErrorResponse error;

    @XmlElement(name = "ID", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getRequestTokenId() {
        return requestTokenId;
    }

    public void setRequestTokenId(String requestTokenId) {
        this.requestTokenId = requestTokenId;
    }

    @XmlElement(name = "Status", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    @XmlElement(name = "HttpStatusCode", namespace = "http://schemas.microsoft.com/windowsazure")
    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    @XmlElement(name = "Error", namespace = "http://schemas.microsoft.com/windowsazure")
    public AzureErrorResponse getError() {
        return error;
    }

    public void setError(AzureErrorResponse error) {
        this.error = error;
    }

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AzureRequestStatusModel [requestTokenId=");
		builder.append(requestTokenId);
		builder.append(", requestStatus=");
		builder.append(requestStatus);
		builder.append(", httpStatusCode=");
		builder.append(httpStatusCode);
		builder.append(", error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}

}
