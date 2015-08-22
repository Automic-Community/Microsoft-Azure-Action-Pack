package com.automic.azure.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;

/**
 * Azure utility class
 * 
 */
public final class CommonUtil {

    private static final String YES = "YES";
    private static final String TRUE = "TRUE";
    private static final String ONE = "1";

    private static final Logger LOGGER = LogManager.getLogger(CommonUtil.class);
  
    private static final String RESPONSE_ERROR = "ERROR";

    private CommonUtil() {
    }

    /**
     * Method to append type to message in format "type | message"
     * 
     * @param type
     * @param message
     * @return
     */
    public static String formatErrorMessage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(RESPONSE_ERROR).append(" | ").append(message);
        return sb.toString();
    }

    /**
     * 
     * Method to get unsigned integer value if presented by a string literal.
     * 
     * @param value
     * @return
     */
    public static int getAndCheckUnsignedValue(String value) {
        int i = -1;
        if (Validator.checkNotEmpty(value)) {
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                i = -1;
            }
        }
        return i;
    }

    /**
     * Method to convert YES/NO values to boolean true or false
     * 
     * @param value
     * @return true if YES, 1
     */
    public static boolean convert2Bool(String value) {
        boolean ret = false;
        if (Validator.checkNotEmpty(value)) {
            String upperCaseValue = value.toUpperCase();
            ret = YES.equals(upperCaseValue) || TRUE.equals(upperCaseValue) || ONE.equals(upperCaseValue);
        }
        return ret;
    }

    /**
     * Method to format a stream of unformatted xml and write it to an output stream.
     * 
     * @param input
     *            xml as a InputStream
     * @param out
     *            Stream to write formatted xml to
     * @param indent
     *            indentation value. usually 2
     * @throws AzureException
     */
    public static void printFormattedXml(InputStream input, OutputStream out, int indent) throws AzureException {
        try {
            Source xmlInput = new StreamSource(new InputStreamReader(input));
            StreamResult xmlOutput = new StreamResult(out);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
        } catch (TransformerException e) {
            LOGGER.error(ExceptionConstants.GENERIC_ERROR_MSG,e);
            throw new AzureException(ExceptionConstants.GENERIC_ERROR_MSG + e.getMessage());
        }
    }
    
    
    /**
     * Get UTC time for Storage Services in "EEE, dd MMM yyyy HH:mm:ss z" format
     * @return Current date as a string
     */
    public static String getCurrentUTCDateForStorageService(){
    	String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    	
		DateFormat rfc1123Format = new SimpleDateFormat(RFC1123_PATTERN);
		rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return rfc1123Format.format(new Date());
    }

}
