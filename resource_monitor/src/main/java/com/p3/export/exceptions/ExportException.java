package com.p3.export.exceptions;

public class ExportException extends Exception {
  public ExportException(final String message) {
    super(message);
  }

  public ExportException(final String message, final Throwable cause) {
    super(message + ": " + cause.getMessage(), cause);
  }
}
