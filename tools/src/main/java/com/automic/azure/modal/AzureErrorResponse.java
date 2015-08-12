package com.automic.azure.modal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

}
