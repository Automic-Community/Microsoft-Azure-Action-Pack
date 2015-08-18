package com.automic.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (requestStatus != null ? "UC4RB_AZR_REQ_STATUS ::=" + requestStatus + "\n" : "")
            + (error != null ? "UC4RB_AZR_REQ_STATUS_ERROR ::=" + error.getCode()+ "\n" : "")
            + (error != null ? "UC4RB_AZR_REQ_STATUS_ERRMSG ::=" + error.getMessage() : "");       
  }

}
