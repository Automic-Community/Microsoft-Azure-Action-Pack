/**
 * 
 */
package com.automic.azure.constants;

/**
 * Enum for Container Access. Could be either CONTAINER or BLOB or PRIVATE
 *
 */
public enum ContainerAccess {
    CONTAINER("container"), BLOB("blob"), PRIVATE("private");

    String value;

    private ContainerAccess(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
