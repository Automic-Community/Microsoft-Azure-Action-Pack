package com.automic.azure.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 
 * Utility Class that has many utility methods to put validations on {@link Object}, {@link String}, {@link File}.
 *
 */
public final class Validator {

    private Validator() {
    }

    /**
     * Method to check if an Object is null
     * 
     * @param field
     * @return true or false
     */
    public static boolean checkNotNull(Object field) {
        return field != null;
    }

    /**
     * Method to check if a String is not empty
     * 
     * @param field
     * @return true if String is not empty else false
     */
    public static boolean checkNotEmpty(String field) {
        return field != null && !field.isEmpty();
    }

    /**
     * Method to check if file represented by a string literal exists or not
     * 
     * @param filePath
     * @return true or false
     */
    public static boolean checkFileExists(String filePath) {
        boolean ret = false;
        if (checkNotEmpty(filePath)) {
            File tmpFile = new File(filePath);
            ret = tmpFile.exists() && tmpFile.isFile();
        }
        return ret;
    }

    /**
     * Method to check if path specified by string literal is a Directory and it exists or not
     * 
     * @param dir
     * @return true or false
     */
    public static boolean checkDirectoryExists(String dir) {
        boolean ret = false;
        if (checkNotEmpty(dir)) {
            File tmpFile = new File(dir);
            ret = tmpFile.exists() && !tmpFile.isFile();
        }
        return ret;
    }

    /**
     * Method to check if Parent Folder exists or not
     * 
     * @param filePath
     * @return true if exists else false
     */
    public static boolean checkFileDirectoryExists(String filePath) {
        boolean ret = false;
        if (checkNotEmpty(filePath)) {
            File tmpFile = new File(filePath);
            File folderPath = tmpFile.getParentFile();
            ret = checkNotNull(folderPath) && folderPath.exists() && !folderPath.isFile();
        }
        return ret;
    }

    /**
     * Method to check if a text matches the given pattern
     * 
     * @param pattern
     *            pattern to match
     * @param text
     *            String to match the pattern
     * @return true or false
     */
    public static boolean isValidText(String pattern, String text) {
        return Pattern.matches(pattern, text);
    }

    /**
     * <p>
     * Method to validate a container name. Rules are as:
     * <ul>
     * <li>length: 3 to 63 characters. numbers, lower-case letters and - only</li>
     * <li>Dash (-) must be immediately preceded and followed by a letter or number</li>
     * </ul>
     * </p>
     * 
     * @param containerName
     * @return true if it is a valid container name else false
     */
    public static boolean isStorageContainerNameValid(String containerName) {
        if (Validator.checkNotEmpty(containerName) && containerName.matches("[0-9a-z-]{3,63}")) {

            if (!containerName.contains("--") && !containerName.startsWith("-") && !containerName.endsWith("-")) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Method to validate a blob name. Rules are as:
     * <ul>
     * <li>cannot end with dot(.) or /</li>
     * <li>cannot contain \\.</li>
     * <li>can hav max 1024 characters only</li>
     * </ul>
     * </p>
     * 
     * @param blobName
     * @return true if it is a valid blob name else false
     */
    public static boolean isContainerBlobNameValid(String blobName) {
        if (Validator.checkNotEmpty(blobName) && !blobName.endsWith(".") && !blobName.endsWith("/")) {

            if (blobName.length() < 1025 && !blobName.contains("\\")) {
                return true;
            }

        }

        return false;
    }
}
