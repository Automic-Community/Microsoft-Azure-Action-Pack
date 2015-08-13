package com.automic.azure.utility;

import com.automic.azure.exceptions.AzureException;

/**
 * This class write byte stream to System output stream
 * 
 * @author anuragupadhyay
 *
 */
public final class ConsoleWriter {

  private ConsoleWriter() {
  }

  /**
   * Method to write specific part of byte array to Stream
   * 
   * @param bytes
   * @param offset
   * @param length
   */
  public static void write(byte[] buf, int off, int len) {
    System.out.write(buf, off, len);
  }

  /**
   * Method to write bytes to Stream
   * 
   * @param bytes
   */
  public static void write(byte[] bytes) {
    write(bytes, 0, bytes.length);
  }

  /**
   * Method to write bytes to Stream with new line
   * 
   * @param bytes
   * @throws AzureException
   */
  public static void writeln(String content) {
    write(content.getBytes());
    newLine();
  }

  public static void write(String content) {
    write(content.getBytes());
  }

  /**
   * Method to write a String to stream
   * 
   * @param field
   * @throws AzureException
   */
  public static void newLine() {
    write(System.lineSeparator().getBytes());
  }

  /**
   * Method to flush to stream
   */
  public static void flush() {
    System.out.flush();

  }

}
