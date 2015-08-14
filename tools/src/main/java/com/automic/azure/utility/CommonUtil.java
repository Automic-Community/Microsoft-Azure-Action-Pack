package com.automic.azure.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

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
   * 
   * @param filePath
   *          file to read
   * @param doDelete
   *          delete file or not
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
   * 
   * @param file
   *          file to read
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
      LOGGER.error(msg, e);
      throw new AzureException(msg, e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException io) {
          LOGGER.error("Exception in closing input stream " + io);
        }
      }
    }
  }

  /**
   * Method to copy contents of an {@link InputStream} to a {@link ByteWriter}
   * 
   * @param source
   *          {@link InputStream} to read from
   * @param dest
   *          {@link ByteWriter} to write to
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
      LOGGER.error(msg, e);
      throw new AzureException(msg, e);
    }
  }

  /**
   * Method to append type to message in format "type | message"
   * 
   * @param type
   * @param message
   * @return
   */
  public static String formatMessage(String type, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append(type).append(" | ").append(message);
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
   * Method parses command line argument against the given Options and provides Map<String,String>
   * which contains key as option short name and value as argument value
   * 
   * @param Options
   *          options
   * @param String
   *          [] args
   * @return Map<String,String>
   */
  public static Map<String, String> getMapFromCmdLine(Options options, String[] args)
      throws AzureException {
    Map<String, String> argsMap = new HashMap<String, String>(10);
    CommandLine cmd = getCommandLine(options, args);

    for (Option option : cmd.getOptions()) {
      String value = option.getValue();
      argsMap.put(option.getOpt(), value);
    }

    return argsMap;

  }

  /**
   * Method provides the CommandLine object for the given Options and command line arguments
   * */
  public static CommandLine getCommandLine(Options options, String[] args) throws AzureException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;

    try {
      cmd = parser.parse(options, args, true);

    } catch (ParseException e) {
      LOGGER.error("Recieved args " + logArgs(args));
      LOGGER.error("Error parsing the command line options", e);
      throw new AzureException(String.format(ExceptionConstants.INVALID_ARGS, e.getMessage()));
    }
    return cmd;
  }

  /**
   * Method extract the action name from the given command line arguments and Options
   * */
  public static String getAction(Options options, String[] args) throws AzureException {

    CommandLine cmd = getCommandLine(options, args);

    if (cmd.hasOption(Constants.ACTION)) {
      return cmd.getOptionValue(Constants.ACTION).toUpperCase();

    } else {
      throw new AzureException(ExceptionConstants.ACTION_MISSING);
    }
  }

  public static void help(String helpHeader, Options options) {
    HelpFormatter formater = new HelpFormatter();

    helpHeader = (helpHeader.isEmpty()) ? ".." : helpHeader;
    formater.printHelp(helpHeader, options);

    System.exit(0);

  }

  private static String logArgs(String[] args) {

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < args.length; i = i + 2) {

      sb.append(args[i]);
      sb.append("=");
      if (!((i + 1) > args.length - 1)) {
        sb.append(args[i + 1]);
      }
      sb.append(" ");
    }
    return sb.toString();
  }

  /**
   * Prints an Object as an error then terminate the line.
   * 
   * @param obj
   *          {@link Object}
   */
  public static void print(String str, Logger log, StandardLevel level) {
    if (Validator.checkNotEmpty(str)) {
      if (StandardLevel.INFO == level) {
        logInfo(str, log);
      } else {
        logError(str, log);
      }
      ConsoleWriter.writeln(str);
    }
  }

  /**
   * this method write Object to logger info
   * 
   * @param obj
   * @param log
   */
  private static void logInfo(Object obj, Logger log) {
    log.info(obj);
  }

  /**
   * This method write Object to Logger error
   * 
   * @param obj
   * @param log
   */
  private static void logError(Object obj, Logger log) {
    log.error(obj);

  }

	/**
	 * Method to format a stream of unformatted xml and write it to an output
	 * stream.
	 * 
	 * @param input
	 *            xml as a InputStream
	 * @param out
	 *            Stream to write formatted xml to
	 * @param indent
	 *            indentation value. usually 2
	 * @throws AzureException
	 */
	public static void printFormattedXml(InputStream input, OutputStream out,
			int indent) throws AzureException {
		try {
			Source xmlInput = new StreamSource(new InputStreamReader(input));
			StreamResult xmlOutput = new StreamResult(out);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
		} catch (TransformerException e) {

			LOGGER.error(ExceptionConstants.GENERIC_ERROR_MSG, e);
			throw new AzureException(ExceptionConstants.GENERIC_ERROR_MSG, e);
		}
	}

}