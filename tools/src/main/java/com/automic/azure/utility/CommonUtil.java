package com.automic.azure.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.spi.StandardLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;

/**
 * Azure utility class 
 * 
 */
public final class CommonUtil {

    private static final String YES = "YES";
    private static final String TRUE = "TRUE";
    private static final String ONE = "1";

    private static final Logger LOGGER = LogManager.getLogger(CommonUtil.class);
    private static final String CHARSET = "UTF-8";
    
    private CommonUtil() {
    }


    /**
     * 
     * Method to read a file and return its content as a String
     * @param filePath file to read
     * @param doDelete delete file or not
     * @return file content as a String
     * @throws IOException
     */
    public static String readFileFromPath(String filePath, boolean doDelete) throws IOException {

        File file = new File(filePath);
        try {
            InputStream in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, CHARSET));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            reader.close();

            return out.toString();
        } finally {
            if (doDelete && !file.delete()) {
                LOGGER.error("Error deleting file " + file.getName());
            }

        }

    }

    /**
     * Method to create file at location filePath. If some error occurs it will delete the file
     * 
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void createFile(String filePath, String content) throws AzureException {

        File file = new File(filePath);

        boolean success = true;
        try (FileWriter fr = new FileWriter(file)) {
            fr.write(content);
        } catch (IOException e) {
            success = false;
            LOGGER.error("Error while writing file ", e);
            throw new AzureException(String.format(ExceptionConstants.UNABLE_TO_WRITE_FILE, filePath), e);
        } finally {
            if (!success && !file.delete()) {
                LOGGER.error("Error deleting file " + file.getName());
            }
        }
    }
    

    /**
     * Method to copy contents of a File to a ByteWriter
     * @param file file to read
     * @param dest 
     * @throws AzureException
     */
    public static void copyData(File file, ByteWriter dest) throws AzureException {
    	InputStream is = null;
    	try {
    		is = new FileInputStream(file);
			copyData(is, dest);
		} catch (FileNotFoundException e) {
			String msg = String.format(ExceptionConstants.UNABLE_TO_COPY_DATA, file);
			LOGGER.error(msg,e);
            throw new AzureException(msg, e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch(IOException io) {
					//log the error
				}
			}
		}
    }
    
    /**
     * Method to copy contents of an {@link InputStream} to a {@link ByteWriter}
     * @param source {@link InputStream} to read from
     * @param dest {@link ByteWriter} to write to
     * @throws AzureException
     */
    public static void copyData(InputStream source, ByteWriter dest) throws AzureException {    	
    	byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
    	int length;
    	try {
			while ((length = source.read(buffer)) > 0) {
				dest.write(buffer, 0, length);
			}
		} catch (IOException e) {
			String msg = String.format(ExceptionConstants.UNABLE_TO_COPY_DATA, source);
			LOGGER.error(msg,e);
            throw new AzureException(msg, e);
		}         
    }
    

    /**
     * Method to append type to message in format "type | message"
     * @param type
     * @param message
     * @return 
     */
    public static String formatMessage(String type, String message) {
        StringBuffer sb = new StringBuffer();
        sb.append(type).append(" | ").append(message);
        return sb.toString();
    }

    /**
     * 
     * Method to get unsigned integer value if presented by a string literal. 
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
     * Method parses command line argument against the given Options and provides Map<String,String>
     * which contains key as option short name and value as argument value
     * @param Options options 
     * @param String[] args
     * @return Map<String,String>
     */
    public static Map<String, String> parseCommandLine(Options options,String[] args) throws AzureException {
    	return parseCommandLine(options,args,"");
    }
    public static Map<String, String> parseCommandLine(Options options,String[] args,String helpHeader) throws AzureException {		
   	 Map<String, String> argsMap = new HashMap<String,String>(10);
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args, true);
			
		} catch (ParseException e) {
			LOGGER.error("Error parsing the command line options ", e);			
			throw new AzureException(String.format(ExceptionConstants.INVALID_ARGS, e.getMessage()));
		}
		
		if(cmd.hasOption('h') || cmd.hasOption("help")){
			help(helpHeader,options);
		}
			for (Option option : cmd.getOptions()) {
				String value = option.getValue().trim();
				if(option.hasArg() && option.isRequired() && value.isEmpty()){
					throw new AzureException(String.format(ExceptionConstants.OPTION_VALUE_MISSING,option.getOpt(),option.getDescription()));
				}
				argsMap.put(option.getOpt(), value);
			}
		
		return argsMap;
	
	}
    
    public static void help(String helpHeader, Options options) {
		HelpFormatter formater = new HelpFormatter();
		
		helpHeader = (helpHeader.isEmpty())?"..":helpHeader;
		formater.printHelp(helpHeader, options);
		
		System.exit(0);
		
	}

    /**
     * Prints an Object and then terminate the line.
     * @param obj {@link Object}
     */
  /*  public static void print(Object obj){    	
    	System.out.println(obj);    	
    }*/
    
    /**
     * Prints an Object as an error then terminate the line.
     * @param obj {@link Object}
     */
    public static void print(Object obj, Logger log, StandardLevel level){ 
    	
    	switch(level){    	
    	case INFO:
    		log.info(obj);
    		break;
    	case ERROR:
    		log.error(obj);
    		break;
    	case OFF:
    		break;
    	default:
    		log.info(obj);    		
    	}
    	System.out.println(obj);    	
    }
    
   /* *//**
     * This method take xml as string and xml representation class as input and return Object of xml representation
     * @param xmlString
     * @param cls
     * @return 
     * @return Object
     * @throws AzureException 
     * @throws JAXBException
     *//*
    public static <T> T xmlToObject(String xmlString, Class<T> cls) throws AzureException { 
    	
              JAXBContext jaxbContext = null;
              T obj = null;
			try {
					jaxbContext = JAXBContext.newInstance(cls);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					obj = (T) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));
			} catch (JAXBException e) {
					LOGGER.error(" Exception in Parsing xml string to object"+e);
					throw new AzureException(e.getMessage());				
			}              
			return obj;
    }*/

    public static String ObjectToXmlString(Object obj, Class<? extends Object> cls) throws JAXBException {      		  
    		StringWriter writer = new StringWriter();
    		JAXBContext context = JAXBContext.newInstance(cls);            
    		Marshaller m = context.createMarshaller();
    		m.marshal(obj, writer);		
    		return  writer.toString(); 	
    }
}
