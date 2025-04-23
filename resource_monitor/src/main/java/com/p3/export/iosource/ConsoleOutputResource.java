package com.p3.export.iosource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleOutputResource implements OutputResource {

  @Override
  public Writer openNewOutputWriter(final Charset charset, final boolean appendOutput)
      throws IOException {
    final Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));
    log.info("Opened output writer to console");
    return new OutputWriter(getDescription(), writer, false);
  }

  private String getDescription() {
    return "<console>";
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
